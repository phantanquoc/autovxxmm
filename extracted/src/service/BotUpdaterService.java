/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import constants.API;
import core.model.Bot;
import core.model.BotObserver;
import core.model.BotStatus;
import core.module.impl.CollectScreen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.Application;
import network.http.Request;
import network.http.Response;
import service.BotService;
import service.ServerService;
import ui.ApplicationUI;
import utils.Res;

public class BotUpdaterService
extends Thread {
    private static final String PATH = API.createUrl("/api/client/bots");
    private static final String COLLECT_BOTS_PATH = API.createUrl("/api/client/bots/collect");
    private static final String COLLECT_BOTS_CHECK_UPDATE_PATH = API.createUrl("/api/client/bots/collect/check-update");
    private static final String COLLECT_BOTS_NEW_PATH = API.createUrl("/api/client/bots/collect/new");
    private static final String COLLECT_BOTS_UPDATED_PATH = API.createUrl("/api/client/bots/collect/updated");
    private static final String COLLECT_BOTS_DELETED_PATH = API.createUrl("/api/client/bots/collect/deleted");
    private static final String COLLECT_CHECK_UPDATE_PATH = API.createUrl("/api/client/collect/check-update");
    private static final String COLLECT_PENDING_PATH = API.createUrl("/api/client/collect/pending");
    private static final String COLLECT_ACK_PATH = API.createUrl("/api/client/collect/ack");
    private static final long CHECK_UPDATE_INTERVAL = 5000L;
    private static final long UPDATE_BOT_DETAILS_INTERVAL = 10000L;
    /** Interval for polling collect-task updates (10 s). */
    private static final long CHECK_COLLECT_INTERVAL = 10000L;
    /** Interval for polling collect-bot list delta (5 s) — picks up bots added/edited/removed via web. */
    private static final long CHECK_COLLECT_BOTS_INTERVAL = 5000L;
    /** Timeout before giving up on an active collect run and ack-ing FAILED (5 min). */
    private static final long COLLECT_TIMEOUT = 300000L;
    private final BotService botService;
    private boolean getBots;
    private long timeCheckUpdate;
    private long timeSendBotDetails;
    private long timeCheckCollect;
    private long timeCheckCollectBots;
    private final List<BotObserver.ObserveResult> observeResults = new ArrayList<BotObserver.ObserveResult>();

    /** Track the currently running collect task so we can ack when done. */
    private int activeCollectTaskId = -1;
    /** Timestamp when the current collect run started (for timeout). */
    private long collectRunStartTime = 0L;
    /** Per-order-bot collected amount for the active run, keyed by ORDER bot id (target). */
    private final Map<Integer, Integer> perBotCollected = new HashMap<Integer, Integer>();
    /** Target order-bot ids snapshotted from the task when it was claimed. */
    private final Set<Integer> activeTaskTargets = new java.util.HashSet<Integer>();
    /** Sum of all perBotCollected entries for the active run. */
    private int activeTotalCollected = 0;

    public BotUpdaterService(BotService botService) {
        this.botService = botService;
    }

    public synchronized void addObserveResult(BotObserver.ObserveResult result) {
        this.removeObserveResult(result);
        this.observeResults.add(result);
    }

    private void removeObserveResult(BotObserver.ObserveResult result) {
        BotObserver.ObserveResult remove = null;
        for (BotObserver.ObserveResult r : this.observeResults) {
            if (r.getId() != result.getId()) continue;
            remove = r;
            break;
        }
        if (remove != null) {
            this.observeResults.remove(remove);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!this.getBots) {
                    this.getBots();
                    this.refreshBotCollects();
                    this.getBots = true;
                } else if (!Application.systemInterrupt) {
                    if (Res.t() > this.timeCheckUpdate) {
                        this.checkUpdate();
                        this.timeCheckUpdate = Res.t() + 5000L;
                    }
                    if (Res.t() > this.timeSendBotDetails) {
                        this.sendBotDetails();
                        this.timeSendBotDetails = Res.t() + 10000L;
                    }
                    if (Res.t() > this.timeCheckCollect) {
                        this.checkCollectTask();
                        this.timeCheckCollect = Res.t() + 10000L;
                    }
                    if (Res.t() > this.timeCheckCollectBots) {
                        this.checkCollectBotsUpdate();
                        this.timeCheckCollectBots = Res.t() + 5000L;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Res.sleep(1000L);
        }
    }

    /**
     * Load collect-role bots from /api/client/bots/collect on startup.
     * Uses Bot.createBotCollect() which attaches BotObserver and starts the connection.
     */
    private void refreshBotCollects() {
        Request request = new Request(COLLECT_BOTS_PATH, "GET");
        Response response = request.send();
        if (response == null || !response.isSuccess()) {
            return;
        }
        JsonArray bots = response.getData().getAsJsonArray();
        for (JsonElement elem : bots) {
            JsonObject obj = elem.getAsJsonObject();
            this.addCollectBotFromJson(obj);
        }
    }

    /** Helper: instantiate + register a collect bot from its JSON payload. */
    private void addCollectBotFromJson(JsonObject obj) {
        this.botService.addBotCollect(Bot.createBotCollect(obj));
    }

    /**
     * Poll /api/client/bots/collect/check-update every 5 s. When the backend
     * reports new/updated/deleted collect bots, fetch the delta and update the
     * in-memory list so that "gom xu" tasks created via web see fresh targets.
     */
    private void checkCollectBotsUpdate() {
        Request checkReq = new Request(COLLECT_BOTS_CHECK_UPDATE_PATH, "GET");
        Response checkResp = checkReq.send();
        if (checkResp == null || !checkResp.isSuccess()) {
            return;
        }
        JsonObject data = checkResp.getData().getAsJsonObject();
        if (data.has("hasNewBots") && data.get("hasNewBots").getAsBoolean()) {
            this.fetchNewCollectBots();
            Res.sleep(500L);
        }
        if (data.has("hasUpdatedBots") && data.get("hasUpdatedBots").getAsBoolean()) {
            this.fetchUpdatedCollectBots();
            Res.sleep(500L);
        }
        if (data.has("hasDeletedBots") && data.get("hasDeletedBots").getAsBoolean()) {
            this.fetchDeletedCollectBots();
        }
    }

    private void fetchNewCollectBots() {
        Request req = new Request(COLLECT_BOTS_NEW_PATH, "GET");
        Response resp = req.send();
        if (resp == null || !resp.isSuccess()) {
            return;
        }
        JsonArray bots = resp.getData().getAsJsonArray();
        for (JsonElement elem : bots) {
            this.addCollectBotFromJson(elem.getAsJsonObject());
        }
    }

    private void fetchUpdatedCollectBots() {
        Request req = new Request(COLLECT_BOTS_UPDATED_PATH, "GET");
        Response resp = req.send();
        if (resp == null || !resp.isSuccess()) {
            return;
        }
        JsonArray bots = resp.getData().getAsJsonArray();
        for (JsonElement elem : bots) {
            JsonObject obj = elem.getAsJsonObject();
            int id = obj.get("id").getAsInt();
            String account = obj.get("account").getAsString();
            String password = obj.get("password").getAsString();
            String charName = obj.get("charName").getAsString();
            int serverId = obj.get("serverId").getAsInt();
            int mapId = obj.get("mapId").getAsInt();
            int zoneId = obj.get("zoneId").getAsInt();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            String manager = obj.get("manager").getAsString().trim();
            String[] chat = Res.split(obj.get("chat").getAsString(), ";");
            String[] sms = Res.split(obj.get("sms").getAsString(), ";");
            boolean enable = obj.get("enable").getAsBoolean();

            Bot existing = null;
            for (Bot b : this.botService.getBotCollects()) {
                if (b.getId() == id) { existing = b; break; }
            }

            // The /collect/updated endpoint fires even when only obsCoin/obsStatus changed
            // (Prisma @updatedAt auto-bumps on every PUT /collect observer push).
            // Skip destroy+recreate unless a config field actually changed; otherwise we'd
            // kick the bot's session every ~5-10 s and it would never finish logging in.
            if (existing != null) {
                boolean identityChanged = !account.equals(existing.getAccount())
                        || !password.equals(existing.getPassword())
                        || !charName.equals(existing.getCharName())
                        || existing.getServer() == null
                        || existing.getServer().getId() != serverId;
                boolean configChanged =
                        existing.getMapId() != mapId
                        || existing.getZoneId() != zoneId
                        || existing.getPosX() != posX
                        || existing.getPosY() != posY
                        || !manager.equals(existing.getManager())
                        || existing.isEnable() != enable
                        || !Arrays.equals(existing.getChat() == null ? new String[0] : existing.getChat(), chat)
                        || !Arrays.equals(existing.getSms() == null ? new String[0] : existing.getSms(), sms);
                if (identityChanged) {
                    // destroy+recreate: credentials or server changed
                    this.botService.removeById(2, id);
                    this.botService.addBotCollect(Bot.createBotCollect(obj));
                } else if (configChanged) {
                    // apply in-place: location/enable/chat/sms changed, no session churn needed
                    boolean wasDisabled = !existing.isEnable();
                    existing.setMapId(mapId);
                    existing.setZoneId(zoneId);
                    existing.setPosX(posX);
                    existing.setPosY(posY);
                    existing.setManager(manager);
                    existing.setChat(chat);
                    existing.setSms(sms);
                    existing.setEnable(enable);
                    if (wasDisabled && enable) {
                        existing.getAutoLogin().reconnect();
                    }
                }
                // else: no functional change (probably just obsCoin bumped) — skip, avoid session churn
            } else {
                // new bot arrived via updated endpoint
                this.botService.addBotCollect(Bot.createBotCollect(obj));
            }
        }
    }

    private void fetchDeletedCollectBots() {
        Request req = new Request(COLLECT_BOTS_DELETED_PATH, "GET");
        Response resp = req.send();
        if (resp == null || !resp.isSuccess()) {
            return;
        }
        JsonArray ids = resp.getData().getAsJsonArray();
        for (JsonElement elem : ids) {
            this.botService.removeById(2, elem.getAsInt());
        }
    }

    /**
     * Poll /api/client/collect/check-update every 10 s.
     * On hasPending=true, claim the task, set CollectScreen.coinKeep, and start all collect bots.
     * If a collect run is already active, check if all targets reported (early-ack) or timed out.
     */
    private void checkCollectTask() {
        List<Bot> collectBots = this.botService.getBotCollects();
        if (collectBots.isEmpty()) {
            return;
        }

        // If a run is currently active, check completion or timeout.
        if (this.activeCollectTaskId != -1) {
            boolean allDone = !this.activeTaskTargets.isEmpty();
            if (allDone) {
                for (Integer targetId : this.activeTaskTargets) {
                    if (!this.isTargetDoneOrUnreachable(targetId.intValue())) {
                        allDone = false;
                        break;
                    }
                }
            }
            boolean timedOut = Res.t() - this.collectRunStartTime >= COLLECT_TIMEOUT;
            // Grace period: if allDone but nothing collected yet, wait 60s before acking FAILED.
            // This prevents race-condition false-FAILED when collect bots haven't finished login.
            boolean pastGrace = this.activeTotalCollected > 0
                || Res.t() - this.collectRunStartTime >= 60000L;
            if ((allDone && pastGrace) || timedOut) {
                String status = this.activeTotalCollected > 0 ? "DONE" : "FAILED";
                boolean acked = this.ackCollectTask(this.activeCollectTaskId, status, this.activeTotalCollected);
                if (acked) {
                    this.stopAllCollectBots(collectBots);
                    this.resetActiveCollectState();
                }
            }
            return;
        }

        // No active run: check if there is a pending task.
        Request checkReq = new Request(COLLECT_CHECK_UPDATE_PATH, "GET");
        Response checkResp = checkReq.send();
        if (checkResp == null || !checkResp.isSuccess()) {
            return;
        }
        JsonObject checkData = checkResp.getData().getAsJsonObject();
        boolean hasPending = checkData.get("hasPending").getAsBoolean();
        if (!hasPending) {
            return;
        }

        // Claim the pending task.
        Request pendingReq = new Request(COLLECT_PENDING_PATH, "GET");
        Response pendingResp = pendingReq.send();
        if (pendingResp == null || !pendingResp.isSuccess()) {
            return;
        }
        JsonObject task = pendingResp.getData().getAsJsonObject();
        int taskId = task.get("id").getAsInt();
        long keep = task.get("keep").getAsLong();

        // Snapshot target ORDER-bot ids from task.targets[].botId
        this.activeTaskTargets.clear();
        this.perBotCollected.clear();
        this.activeTotalCollected = 0;
        if (task.has("targets")) {
            JsonArray targets = task.get("targets").getAsJsonArray();
            for (JsonElement t : targets) {
                JsonObject obj = t.getAsJsonObject();
                if (obj.has("botId")) {
                    this.activeTaskTargets.add(obj.get("botId").getAsInt());
                }
            }
        }

        // Apply keep value to all collect bots via the shared static field.
        // Push targets whitelist to CollectScreen so onAliveActivities only collects listed bots.
        CollectScreen.activeTargets.clear();
        CollectScreen.activeTargets.addAll(this.activeTaskTargets);
        CollectScreen.coinKeep = keep;

        // Start collecting on all collect bots.
        this.activeCollectTaskId = taskId;
        this.collectRunStartTime = Res.t();
        for (Bot bot : collectBots) {
            if (bot.getScreen() instanceof CollectScreen) {
                ((CollectScreen) bot.getScreen()).onCollect(true);
            }
        }
    }

    /**
     * Called by CollectTrade.success() when a collect bot finishes trading with one ORDER bot.
     * Accumulates per-target coins and the running total for the active task.
     */
    public synchronized void recordCollectSuccess(int orderBotId, int coins) {
        if (this.activeCollectTaskId == -1 || coins <= 0) {
            return;
        }
        Integer prev = this.perBotCollected.get(orderBotId);
        int sum = (prev != null ? prev.intValue() : 0) + coins;
        this.perBotCollected.put(orderBotId, sum);
        this.activeTotalCollected += coins;
    }

    private void resetActiveCollectState() {
        this.activeCollectTaskId = -1;
        this.collectRunStartTime = 0L;
        this.perBotCollected.clear();
        this.activeTaskTargets.clear();
        this.activeTotalCollected = 0;
        CollectScreen.activeTargets.clear();
    }

    /**
     * Coi 1 target là "done" nếu đã gom thành công HOẶC không khả thi (bot order không tồn tại,
     * offline, hoặc không có collect bot online ở cùng server). Tránh treo task 5 phút khi
     * 1 bot trong list offline.
     */
    private boolean isTargetDoneOrUnreachable(int botId) {
        if (this.perBotCollected.containsKey(Integer.valueOf(botId))) return true;
        Bot orderBot = this.botService.getBotOrder(botId);
        if (orderBot == null) return true;
        if (!orderBot.isOnline()) return true;
        boolean hasCollectOnSameServer = false;
        for (Bot collectBot : this.botService.getBotCollects()) {
            if (collectBot.getStatus() != BotStatus.OFFLINE
                && collectBot.getServer() != null
                && orderBot.getServer() != null
                && collectBot.getServer().getId() == orderBot.getServer().getId()) {
                hasCollectOnSameServer = true;
                break;
            }
        }
        return !hasCollectOnSameServer;
    }

    /**
     * Stop all collect bots by calling onCollect(false) on their CollectScreen.
     */
    private void stopAllCollectBots(List<Bot> collectBots) {
        for (Bot bot : collectBots) {
            if (bot.getScreen() instanceof CollectScreen) {
                ((CollectScreen) bot.getScreen()).onCollect(false);
            }
        }
    }

    /**
     * Acknowledge a completed or failed collect task.
     * perBotResults are derived from the in-memory perBotCollected map populated by
     * recordCollectSuccess() during the run.
     */
    private boolean ackCollectTask(int taskId, String status, int totalCollected) {
        try {
            JsonArray perBotResults = new JsonArray();
            for (Map.Entry<Integer, Integer> entry : this.perBotCollected.entrySet()) {
                JsonObject row = new JsonObject();
                row.addProperty("botId", (Number) entry.getKey());
                row.addProperty("collected", (Number) entry.getValue());
                perBotResults.add((JsonElement) row);
            }
            JsonObject body = new JsonObject();
            body.addProperty("taskId", taskId);
            body.addProperty("status", status);
            body.addProperty("totalCollected", totalCollected);
            body.add("perBotResults", perBotResults);
            Request ackReq = new Request(COLLECT_ACK_PATH, "POST");
            Response response = ackReq.send((JsonElement) body);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getBots() {
        Request request = this.createRequest(PATH, "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray bots = response.getData().getAsJsonArray();
            this.createBots(bots);
        }
    }

    private void getNewBots() {
        Request request = this.createRequest(PATH, "/new", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray bots = response.getData().getAsJsonArray();
            this.createBots(bots);
        }
    }

    private void createBots(JsonArray botArray) {
        for (JsonElement botElem : botArray) {
            JsonObject botObj = (JsonObject)botElem;
            Bot bot = Bot.createBotOrder(botObj);
            this.botService.addBotOrder(bot);
        }
        ApplicationUI.getInstance().setBotOrderChanged(true);
        ApplicationUI.getInstance().calculateTotalPageAndUpdate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendBotDetails() {
        // Snapshot + drain queue dưới CÙNG monitor với addObserveResult (this),
        // rồi nhả lock TRƯỚC KHI gọi network để game-thread không bị block trên
        // một PUT chậm/lỗi và tránh ConcurrentModificationException.
        List<BotObserver.ObserveResult> batch;
        synchronized (this) {
            if (this.observeResults.isEmpty()) {
                return;
            }
            batch = new ArrayList<BotObserver.ObserveResult>(this.observeResults);
            this.observeResults.clear();
        }

        // Split batch theo role: COLLECT bots PUT /collect, ORDER bots PUT /normal|/split.
        List<Bot> collectBots = this.botService.getBotCollects();
        JsonArray collectJson = new JsonArray();
        JsonArray orderJson = new JsonArray();
        List<BotObserver.ObserveResult> orderBatch = new ArrayList<BotObserver.ObserveResult>();
        List<BotObserver.ObserveResult> collectBatch = new ArrayList<BotObserver.ObserveResult>();
        for (BotObserver.ObserveResult r : batch) {
            boolean isCollect = false;
            for (Bot b : collectBots) {
                if (b.getId() == r.getId()) { isCollect = true; break; }
            }
            if (isCollect) {
                collectJson.add((JsonElement) r.toJson());
                collectBatch.add(r);
            } else {
                orderJson.add((JsonElement) r.toJson());
                orderBatch.add(r);
            }
        }

        // On failure re-enqueue: BotObserver đã advance cache `this.coin` khi enqueue,
        // nên nếu bỏ payload rớt thì web kẹt giá trị cũ tới khi xu đổi lần nữa.
        if (orderJson.size() > 0) {
            Request request = this.createRequest(PATH, "PUT");
            Response response = request.send((JsonElement) orderJson);
            if (response == null || !response.isSuccess()) {
                this.requeueFailed(orderBatch);
            }
        }
        if (collectJson.size() > 0) {
            Request request = new Request(COLLECT_BOTS_PATH, "PUT");
            Response response = request.send((JsonElement) collectJson);
            if (response == null || !response.isSuccess()) {
                this.requeueFailed(collectBatch);
            }
        }
    }

    /**
     * Đưa các observe-result gửi hụt trở lại queue để retry ở chu kỳ sau (10s),
     * nhưng chỉ khi CHƯA có snapshot mới hơn cho cùng bot id được enqueue trong lúc
     * PUT đang bay — giá trị mới hơn luôn thắng.
     */
    private synchronized void requeueFailed(List<BotObserver.ObserveResult> failed) {
        for (BotObserver.ObserveResult r : failed) {
            boolean superseded = false;
            for (BotObserver.ObserveResult pending : this.observeResults) {
                if (pending.getId() == r.getId()) { superseded = true; break; }
            }
            if (!superseded) {
                this.observeResults.add(r);
            }
        }
    }

    private void getBotUpdates() {
        Request request = this.createRequest(PATH, "/updated", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray updateArray = response.getData().getAsJsonArray();
            for (JsonElement updateElem : updateArray) {
                JsonObject updateObj = updateElem.getAsJsonObject();
                this.handleBotUpdate(updateObj);
            }
        }
    }

    private void handleBotUpdate(JsonObject updateObj) {
        int botId = updateObj.get("id").getAsInt();
        String account = updateObj.get("account").getAsString();
        String password = updateObj.get("password").getAsString();
        String charName = updateObj.get("charName").getAsString();
        int serverId = updateObj.get("serverId").getAsInt();
        boolean enable = updateObj.get("enable").getAsBoolean();
        int mapId = updateObj.get("mapId").getAsInt();
        int zoneId = updateObj.get("zoneId").getAsInt();
        int posX = updateObj.get("posX").getAsInt();
        int posY = updateObj.get("posY").getAsInt();
        String manager = updateObj.get("manager").getAsString().trim();
        String[] chat = Res.split(updateObj.get("chat").getAsString(), ";");
        String[] sms = Res.split(updateObj.get("sms").getAsString(), ";");
        int playFee = updateObj.get("playFee").getAsInt();
        byte typeLuckyDraw = updateObj.get("typeLuckyDraw").getAsByte();
        Bot botOrder = this.botService.getBotOrder(botId);
        if (botOrder != null) {
            boolean locationHasChange;
            boolean reconnectToApply = false;
            boolean bl = locationHasChange = botOrder.getMapId() != mapId || botOrder.getZoneId() != zoneId;
            if (!account.equals(botOrder.getAccount())) {
                botOrder.setAccount(account);
                reconnectToApply = true;
            }
            if (!password.equals(botOrder.getPassword())) {
                botOrder.setPassword(password);
                reconnectToApply = true;
            }
            if (!charName.equals(botOrder.getCharName())) {
                botOrder.setCharName(charName);
                reconnectToApply = true;
            }
            if (serverId != botOrder.getServer().getId()) {
                botOrder.setServer(ServerService.getInstance().getServer(serverId));
                reconnectToApply = true;
            }
            if (enable != botOrder.isEnable()) {
                if (!botOrder.isEnable()) {
                    reconnectToApply = true;
                }
                botOrder.setEnable(enable);
            }
            if (locationHasChange) {
                this.botService.unindexBotOrderLocation(botOrder);
                botOrder.setMapId(mapId);
                botOrder.setZoneId(zoneId);
                this.botService.indexBotOrderLocation(botOrder);
            }
            botOrder.setPosX(posX);
            botOrder.setPosY(posY);
            botOrder.setManager(manager);
            botOrder.setChat(chat);
            botOrder.setSms(sms);
            botOrder.setPlayFee(playFee);
            botOrder.setTypeLuckyDraw(typeLuckyDraw);
            if (reconnectToApply) {
                botOrder.getAutoLogin().reconnect();
            }
        }
    }

    private void getDeletedBots() {
        Request request = this.createRequest(PATH, "/deleted", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray responseData = response.getData().getAsJsonArray();
            int count = 0;
            for (JsonElement botIdElement : responseData) {
                int id = botIdElement.getAsInt();
                if (!this.botService.removeById(0, id)) continue;
                ++count;
            }
            if (count > 0) {
                ApplicationUI.getInstance().setBotOrderChanged(true);
                ApplicationUI.getInstance().calculateTotalPageAndUpdate();
            }
        }
    }

    private void getClientChangedBotsToDelete() {
        Request request = this.createRequest(PATH, "/changed/delete", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray botsToDelete = response.getData().getAsJsonArray();
            botsToDelete.forEach(bot -> this.botService.removeById(0, bot.getAsInt()));
        }
    }

    private void getClientChangedNewBots() {
        Request request = this.createRequest(PATH, "/changed/new", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray newBots = response.getData().getAsJsonArray();
            this.createBots(newBots);
        }
    }

    public void clientExit() {
        Request request = this.createRequest(PATH, "/exit", "PUT");
        request.send();
    }

    private void checkUpdate() {
        Request request = this.createRequest(PATH, "/check-update", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonObject responseData = response.getData().getAsJsonObject();
            if (responseData.get("hasNewBots").getAsBoolean()) {
                this.getNewBots();
                Res.sleep(1000L);
            }
            if (responseData.get("hasUpdatedBots").getAsBoolean()) {
                this.getBotUpdates();
                Res.sleep(1000L);
            }
            if (responseData.get("hasDeletedBots").getAsBoolean()) {
                this.getDeletedBots();
                Res.sleep(1000L);
            }
            if (Application.isSplitClient) {
                if (responseData.get("hasChangedClientBotsToDelete").getAsBoolean()) {
                    this.getClientChangedBotsToDelete();
                    Res.sleep(1000L);
                }
                if (responseData.get("hasChangedClientNewBots").getAsBoolean()) {
                    this.getClientChangedNewBots();
                    Res.sleep(1000L);
                }
            }
        }
    }

    private Request createRequest(String path, String method) {
        return this.createRequest(path, null, method);
    }

    private Request createRequest(String path, String uri, String method) {
        StringBuilder pathBuilder = new StringBuilder(path);
        if (Application.isSplitClient) {
            pathBuilder.append("/split");
        } else {
            pathBuilder.append("/normal");
        }
        if (uri != null) {
            pathBuilder.append(uri);
        }
        Request request = new Request(pathBuilder.toString(), method);
        if (Application.isSplitClient) {
            request.getHeaders().add("client", String.valueOf(Application.client));
        }
        return request;
    }

    public boolean isGetBots() {
        return this.getBots;
    }
}
