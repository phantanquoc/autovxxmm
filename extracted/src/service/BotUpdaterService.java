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
import java.util.ArrayList;
import java.util.List;
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
    private static final long CHECK_UPDATE_INTERVAL = 5000L;
    private static final long UPDATE_BOT_DETAILS_INTERVAL = 10000L;
    private final BotService botService;
    private boolean getBots;
    private long timeCheckUpdate;
    private long timeSendBotDetails;
    private final List<BotObserver.ObserveResult> observeResults = new ArrayList<BotObserver.ObserveResult>();

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
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Res.sleep(1000L);
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
        if (!this.observeResults.isEmpty()) {
            List<BotObserver.ObserveResult> list = this.observeResults;
            synchronized (list) {
                JsonArray requestJson = new JsonArray();
                this.observeResults.forEach(r -> requestJson.add((JsonElement)r.toJson()));
                Request request = this.createRequest(PATH, "PUT");
                request.send((JsonElement)requestJson);
                this.observeResults.clear();
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

