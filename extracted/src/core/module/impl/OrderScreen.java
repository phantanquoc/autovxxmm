/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.model.Mob;
import core.model.Npc;
import core.model.Order;
import core.model.Party;
import core.model.TileMap;
import core.module.GameScreen;
import core.service.LuckyDraw;
import core.service.NextMap;
import java.util.List;
import lib.mVector;
import service.BotService;
import service.OrderService;
import service.SettingService;
import utils.Res;

public class OrderScreen
extends GameScreen {
    private static SettingService setting = SettingService.getInstance();
    private int mapSaveReturnPoint = -1;
    private int indexPublicChat;
    private long disTimeSendPublicChat;
    private Bot botParty;
    private boolean zoneIsFull;
    private boolean aloneParty;
    private long timeAloneParty;
    private Order order;
    public int coinAtBet;
    private int waitCount;
    private boolean sendFinalResult;
    private boolean waitNewTurn;
    private boolean hasBet;
    private boolean keepBotOnline;
    private long lastKeepBotOnline;
    private boolean isStartKeepBotOnline;
    private long timeStartKeepBotOnline;

    public OrderScreen(Bot bot) {
        super(bot);
    }

    @Override
    protected void onAliveActivities() {
        if (this.keepBotOnline) {
            this.keepBotOnline();
            return;
        }
        if (this.saveReturnPoint()) {
            return;
        }
        if (!this.compareMapAndZone(this.bot.getMapId(), this.bot.getZoneId())) {
            this.changeMapAndZone(this.bot.getMapId(), this.bot.getZoneId());
            return;
        }
        if (this.bot.getTrade().isShow()) {
            this.bot.getTrade().update();
            return;
        }
        if (this.bot.getMyChar().getPosX() != this.bot.getPosX() || this.bot.getMyChar().getPosY() != this.bot.getPosY()) {
            this.move(this.bot.getPosX(), this.bot.getPosY());
        }
        this.autoSendPublicChat();
        this.updateOrder();
        if (this.order == null && setting.isEnableKeepBotOnline() && Res.t() - this.lastKeepBotOnline >= setting.getTimeKeepBotOnline()) {
            OrderScreen.keepOnline(this, this.bot);
        }
    }

    @Override
    public void onDieEvent() {
        if (this.isStartKeepBotOnline) {
            this.endKeepBotOnline();
        }
    }

    private void endKeepBotOnline() {
        this.keepBotOnline = false;
        this.isStartKeepBotOnline = false;
        this.lastKeepBotOnline = Res.t();
        this.bot.debug("K\u1ebft th\u00fac gi\u1eef bot online!");
    }

    private void keepBotOnline() {
        Mob mob;
        boolean isSchoolOrVillageMap;
        boolean bl = isSchoolOrVillageMap = NextMap.isSchool(this.bot.getTileMap().getMapId()) || NextMap.isVillage(this.bot.getTileMap().getMapId());
        if (isSchoolOrVillageMap) {
            this.bot.getNextMap().moveToHole(0);
        }
        if ((mob = this.findFirstMobAlive()) != null) {
            if (!this.isStartKeepBotOnline) {
                this.timeStartKeepBotOnline = Res.t();
                this.isStartKeepBotOnline = true;
            }
            this.move(mob.x, mob.y);
            Res.sleep(500L);
            this.bot.getConnection().getService().sendPlayerAttackMob(mob.mobId);
            Res.sleep(500L);
        }
        if (this.isStartKeepBotOnline && Res.t() - this.timeStartKeepBotOnline >= 1000L) {
            this.endKeepBotOnline();
        }
    }

    private static synchronized void keepOnline(OrderScreen orderScreen, Bot bot) {
        List<Bot> botOrders = BotService.getInstance().getBotOrdersByLocation(bot.getMapId(), bot.getZoneId());
        int onlineCount = 0;
        int keepMapCount = 0;
        for (Bot botOrder : botOrders) {
            if (botOrder.equals(bot) || !botOrder.isOnline()) continue;
            ++onlineCount;
            if (!botOrder.isMapValid() || botOrder.getScreen().orderScreen().keepBotOnline) continue;
            ++keepMapCount;
        }
        if (onlineCount == 0 || keepMapCount >= 1) {
            orderScreen.keepBotOnline = true;
            orderScreen.isStartKeepBotOnline = false;
            bot.debug("B\u1eaft \u0111\u1ea7u gi\u1eef bot online!");
        }
    }

    private boolean saveReturnPoint() {
        if (SettingService.getInstance().isSaveReturnPoint()) {
            boolean flag = true;
            if (this.mapSaveReturnPoint == -1) {
                this.dumpToDie();
                if (this.bot.getMyChar().isDie()) {
                    this.bot.getConnection().getService().returnTownFromDead();
                    this.bot.getWaitAction().waitMap();
                    if (!this.bot.getMyChar().isDie()) {
                        this.mapSaveReturnPoint = this.bot.getTileMap().getMapId();
                        if (this.mapSaveReturnPoint == SettingService.getInstance().getMapSaveReturnPoint()) {
                            flag = false;
                        }
                    }
                }
            } else if (this.mapSaveReturnPoint != SettingService.getInstance().getMapSaveReturnPoint()) {
                this.bot.debug("Ti\u1ebfn h\u00e0nh l\u01b0u to\u1ea1 \u0111\u1ed9 t\u1ea1i map: " + SettingService.getInstance().getMapSaveReturnPoint());
                this.changeMap(SettingService.getInstance().getMapSaveReturnPoint());
                if (this.bot.getTileMap().getMapId() == SettingService.getInstance().getMapSaveReturnPoint()) {
                    Npc npc = this.findNpcInMap(5);
                    if (npc == null) {
                        this.bot.debug("Kh\u00f4ng t\u00ecm th\u1ea5y npc l\u01b0u to\u1ea1 \u0111\u1ed9");
                    } else {
                        this.move(npc.getPosX(), npc.getPosY());
                        Res.sleep(2000L);
                        this.bot.getConnection().getService().menu(5, 1, 0);
                        this.bot.getWaitAction().waitSaveMapReturnTown();
                        if (this.mapSaveReturnPoint == SettingService.getInstance().getMapSaveReturnPoint()) {
                            flag = false;
                        } else {
                            this.bot.debug("L\u01b0u to\u1ea1 \u0111\u1ed9 th\u1ea5t b\u1ea1i!");
                        }
                    }
                }
            } else {
                flag = false;
            }
            return flag;
        }
        return false;
    }

    private void autoSendPublicChat() {
        if (this.bot.getChat() != null && this.bot.getChat().length > 0 && Res.t() - this.disTimeSendPublicChat > 10000L) {
            if (this.indexPublicChat >= this.bot.getChat().length) {
                this.indexPublicChat = 0;
            }
            this.bot.getConnection().getService().chat(this.bot.getChat()[this.indexPublicChat++]);
            this.disTimeSendPublicChat = Res.t();
        }
    }

    private void updateOrder() {
        if (this.order == null || !this.order.hasStatus(0, 1)) {
            this.closeLuckyDraw();
            return;
        }
        if (this.order.hasStatus(0)) {
            this.updateOrderWait();
        } else {
            this.updateOrderBet();
        }
    }

    private void closeLuckyDraw() {
        if (this.bot.getLuckyDraw().isShow) {
            this.bot.getLuckyDraw().close();
        }
        if (this.order == null && this.sendFinalResult) {
            this.sendFinalResult = false;
        }
    }

    private void updateOrderWait() {
        LuckyDraw luckyDraw = this.bot.getLuckyDraw();
        if (!luckyDraw.isShow) {
            this.handleOpenLuckyDraw();
            return;
        }
        if (this.refreshLuckyDraw()) {
            int curSecond = luckyDraw.curSecond();
            int secondBet = Math.max(this.order.getSecond(), 15);
            if (luckyDraw.myCoinJoin > 0) {
                this.updateOrderHasBet(this.order);
            } else if (this.waitNewTurn) {
                if (curSecond >= 10) {
                    this.waitNewTurn = false;
                }
            } else if (curSecond < 10) {
                ++this.waitCount;
                if (this.waitCount >= 3) {
                    this.bot.getConnection().getService().chatPrivate(this.order.getName(), "bot kh\u00f4ng th\u1ec3 \u0111\u1eb7t c\u01b0\u1ee3c trong v\u00f2ng 3 v\u00e1n li\u00ean ti\u1ebfp, vui l\u00f2ng li\u00ean h\u1ec7 admin \u0111\u1ec3 \u0111\u01b0\u1ee3c gi\u1ea3i quy\u1ebft!");
                    if (SettingService.getInstance().isEnableMessageOrderError()) {
                        this.bot.getConnection().getService().chatPrivate(this.order.getName(), SettingService.getInstance().getMessageOrderError());
                    }
                    OrderService.getInstance().error(this.order.getId(), "Kh\u00f4ng th\u1ec3 \u0111\u1eb7t c\u01b0\u1ee3c trong 3 v\u00e1n li\u00ean ti\u1ebfp");
                    this.resetOrder();
                    return;
                }
                if (this.hasBet) {
                    this.bot.getConnection().getService().chatPrivate(this.order.getName(), "kh\u00f4ng th\u1ec3 tham gia \u0111\u1eb7t c\u01b0\u1ee3c v\u00e0o v\u00e1n n\u00e0y, vui l\u00f2ng ch\u1edd v\u00e1n ti\u1ebfp theo!");
                    OrderService.getInstance().log(this.order.getId(), "\u0110\u1eb7t c\u01b0\u1ee3c th\u1ea5t b\u1ea1i");
                } else {
                    this.bot.getConnection().getService().chatPrivate(this.order.getName(), "th\u1eddi gian \u0111\u1eb7t c\u01b0\u1ee3c v\u00e1n n\u00e0y \u0111\u00e3 h\u1ebft, vui l\u00f2ng ch\u1edd v\u00e1n ti\u1ebfp theo!");
                    OrderService.getInstance().log(this.order.getId(), "Th\u1eddi gian \u0111\u1eb7t c\u01b0\u1ee3c \u0111\u00e3 h\u1ebft");
                }
                this.waitNewTurn = true;
            } else if (curSecond <= secondBet) {
                luckyDraw.bet(this.order.getCoinOrder());
                this.hasBet = true;
                if (luckyDraw.myCoinJoin > 0) {
                    this.updateOrderHasBet(this.order);
                } else {
                    Res.sleep(1000L);
                }
            }
        } else {
            this.hasBet = false;
            this.bot.getConnection().getService().chatPrivate(this.order.getName(), "c\u00f3 l\u1ed7i x\u1ea3y ra (2), ch\u1edd bot k\u1ebft n\u1ed1i l\u1ea1i!");
            this.bot.debug("L\u00e0m m\u1edbi v\u00f2ng xoay th\u1ea5t b\u1ea1i (qu\u00e1 15 gi\u00e2y), ti\u1ebfn h\u00e0nh k\u1ebft n\u1ed1i l\u1ea1i");
            this.bot.getAutoLogin().reconnect();
            Res.sleep(2000L);
        }
    }

    private void updateOrderHasBet(Order order) {
        order.setStatus(1);
        this.coinAtBet = this.bot.getMyChar().getCoin();
        this.bot.getConnection().getService().chatPrivate(order.getName(), "bot \u0111\u00e3 \u0111\u1eb7t c\u01b0\u1ee3c th\u00e0nh c\u00f4ng " + Res.moneyFormat(order.getCoinOrder()) + " xu v\u00e0o v\u00f2ng xoay may m\u1eafn!");
        if (SettingService.getInstance().isEnableMessageBet() && !SettingService.getInstance().getMessageBet().isEmpty()) {
            this.bot.getConnection().getService().chatPrivate(order.getName(), SettingService.getInstance().getMessageBet());
        }
        OrderService.getInstance().bet(order.getId());
    }

    private void handleOpenLuckyDraw() {
        LuckyDraw luckyDraw = this.bot.getLuckyDraw();
        int time = 0;
        boolean success = false;
        while (!success && time < 3) {
            if (!luckyDraw.open()) {
                this.bot.debug("M\u1edf v\u00f2ng xoay may m\u1eafn th\u1ea5t b\u1ea1i, th\u1eddi gian ch\u1edd qu\u00e1 5 gi\u00e2y!");
                ++time;
                continue;
            }
            success = true;
        }
        if (!success) {
            this.bot.getConnection().getService().chatPrivate(this.order.getName(), "c\u00f3 l\u1ed7i x\u1ea3y ra (1), ch\u1edd bot k\u1ebft n\u1ed1i l\u1ea1i!");
            this.bot.debug("M\u1edf v\u00f2ng xoay may m\u1eafn th\u1ea5t b\u1ea1i (qu\u00e1 15 gi\u00e2y), ti\u1ebfn h\u00e0nh k\u1ebft n\u1ed1i l\u1ea1i");
            this.bot.getAutoLogin().reconnect();
            Res.sleep(2000L);
        }
    }

    private boolean refreshLuckyDraw() {
        LuckyDraw luckyDraw = this.bot.getLuckyDraw();
        int time = 0;
        boolean result = false;
        while (!result && time < 3) {
            if (!luckyDraw.refresh()) {
                ++time;
                continue;
            }
            result = true;
        }
        return result;
    }

    private void updateOrderBet() {
        LuckyDraw luckyDraw = this.bot.getLuckyDraw();
        if (this.refreshLuckyDraw()) {
            long curSecond = luckyDraw.curSecond();
            if (luckyDraw.myCoinJoin == this.order.getCoinOrder()) {
                if (curSecond <= 5L && !this.sendFinalResult) {
                    this.bot.getConnection().getService().chatPrivate(this.order.getName(), "v\u00f2ng xoay \u0111\u00e3 kho\u00e1 \u0111\u1eb7t c\u01b0\u1ee3c, vui l\u00f2ng ch\u1edd k\u1ebft qu\u1ea3\n-s\u1ed1 xu tham gia: " + Res.moneyFormat(luckyDraw.myCoinJoin) + "\n-t\u1ec9 l\u1ec7 th\u1eafng: " + luckyDraw.percentWin);
                    this.sendFinalResult = true;
                }
            } else {
                String message;
                String name = this.order.getName();
                String optionalMessage = null;
                int coinWin = this.bot.getMyChar().getCoin() - this.coinAtBet;
                if (coinWin > 0) {
                    int coinFee = (int)((long)coinWin * (long)this.bot.getPlayFee() / 100L);
                    int coinReward = coinWin - coinFee;
                    this.order.setStatus(3);
                    this.order.setCoinWin(coinWin);
                    this.order.setCoinFee(coinFee);
                    this.order.setCoinReward(coinReward);
                    message = String.format("ch\u00fac m\u1eebng b\u1ea1n \u0111\u00e3 chi\u1ebfn th\u1eafng %s xu, ph\u00ed ch\u01a1i %d%% = %s xu, b\u1ea1n s\u1ebd nh\u1eadn \u0111\u01b0\u1ee3c %s xu", Res.moneyFormat(coinWin), this.bot.getPlayFee(), Res.moneyFormat(coinFee), Res.moneyFormat(coinReward));
                    if (SettingService.getInstance().isEnableMessageWin()) {
                        optionalMessage = SettingService.getInstance().getMessageWin();
                    }
                    OrderService.getInstance().win(this.order.getId(), this.order.getCoinWin(), this.order.getCoinFee(), this.order.getCoinReward());
                } else {
                    message = "chi\u1ebfn th\u1eafng thu\u1ed9c v\u1ec1 " + luckyDraw.winName + ", ch\u00fac b\u1ea1n may m\u1eafn l\u1ea7n sau!";
                    if (SettingService.getInstance().isEnableMessageLose()) {
                        optionalMessage = SettingService.getInstance().getMessageLose();
                    }
                    OrderService.getInstance().lose(this.order.getId(), luckyDraw.winName);
                    this.resetOrder();
                }
                this.bot.getConnection().getService().chatPrivate(name, message);
                if (optionalMessage != null && !optionalMessage.isEmpty()) {
                    this.bot.getConnection().getService().chatPrivate(name, optionalMessage);
                }
            }
        }
    }

    public void startOrder(Order order) {
        this.order = order;
        this.coinAtBet = 0;
        this.waitCount = 0;
        this.sendFinalResult = false;
        this.waitNewTurn = false;
        this.hasBet = false;
    }

    public void resetOrder() {
        this.order = null;
        this.coinAtBet = 0;
        this.waitCount = 0;
        this.sendFinalResult = false;
        this.waitNewTurn = false;
        this.hasBet = false;
    }

    @Override
    public final void updateParty() {
        if (this.bot.isMapValid()) {
            mVector party = this.bot.getParty();
            if (!party.isEmpty()) {
                Party leader = (Party)party.firstElement();
                boolean isLeader = leader.name.equals(this.bot.getMyChar().getName());
                boolean hasKick = false;
                if (isLeader) {
                    for (int i = 1; i < party.size(); ++i) {
                        Party member = (Party)party.elementAt(i);
                        if (member == null || member.c == null && Res.t() - member.lastTimeParty <= 30000L) continue;
                        this.bot.getConnection().getService().kickOutParty(i);
                        Res.sleep(500L);
                        break;
                    }
                    if (party.size() == 1) {
                        if (!this.aloneParty) {
                            this.timeAloneParty = Res.t();
                            this.aloneParty = true;
                        } else if (Res.t() - this.timeAloneParty >= 30000L) {
                            this.bot.getConnection().getService().leaveParty();
                            this.aloneParty = false;
                            Res.sleep(500L);
                        }
                    } else {
                        this.aloneParty = false;
                    }
                } else if (this.bot.isMapValid()) {
                    boolean leave = true;
                    for (int i = 0; i < party.size(); ++i) {
                        Party member = (Party)party.elementAt(i);
                        if (member == null || member.c != null || member.name.equals(this.bot.getMyChar().getName())) continue;
                        leave = false;
                        break;
                    }
                    if (leave) {
                        this.bot.getConnection().getService().leaveParty();
                        Res.sleep(500L);
                    }
                }
            }
        } else if (this.zoneIsFull) {
            boolean shouldFind;
            this.zoneIsFull = false;
            boolean bl = shouldFind = this.botParty == null || this.botParty.getServer().getId() != this.bot.getServer().getId() || !this.botParty.isOnline() || !this.botParty.isMapValid() || this.botParty.getMapId() != this.bot.getMapId() || this.botParty.getZoneId() != this.bot.getZoneId() || !this.botParty.getParty().isEmpty() && (!this.sameParty(this.botParty.getMyChar().getName()) || !this.botParty.isLeaderOfParty() || this.botParty.getParty().size() >= 6);
            if (shouldFind) {
                Bot findResult = this.findBotParty(this.botParty, this.bot.getServer().getId(), this.bot.getMapId(), this.bot.getZoneId());
                if (findResult != null) {
                    this.botParty = findResult;
                } else {
                    this.botParty = null;
                    return;
                }
            }
            if (!this.sameParty(this.botParty.getMyChar().getName())) {
                if (this.botParty.getParty().isEmpty()) {
                    boolean createNewParty = false;
                    boolean leaveParty = false;
                    if (this.bot.getParty().isEmpty()) {
                        createNewParty = true;
                        Res.sleep(500L);
                    } else if (!this.bot.isLeaderOfParty()) {
                        createNewParty = true;
                        leaveParty = true;
                        Res.sleep(500L);
                    } else if (this.bot.getParty().size() >= 6) {
                        createNewParty = true;
                        leaveParty = true;
                        Res.sleep(500L);
                    }
                    if (leaveParty) {
                        this.bot.getConnection().getService().leaveParty();
                        Res.sleep(500L);
                    }
                    if (createNewParty) {
                        this.bot.getConnection().getService().createParty();
                        Res.sleep(500L);
                    }
                    this.bot.getConnection().getService().inviteIntoParty(this.botParty.getMyChar().getName());
                    Res.sleep(500L);
                } else if (this.botParty.isLeaderOfParty()) {
                    if (!this.bot.getParty().isEmpty()) {
                        this.bot.getConnection().getService().leaveParty();
                        Res.sleep(500L);
                    }
                    this.bot.getConnection().getService().requestJoinParty(this.botParty.getMyChar().getName());
                    Res.sleep(500L);
                }
            }
        }
    }

    @Override
    public final void handleRequestInviteIntoParty(String name, int id) {
        if (BotService.getInstance().exist(this.bot.getServer().getId(), name, 0) || this.bot.getManager().equals(name)) {
            this.bot.getConnection().getService().acceptRequestInviteIntoParty(id);
        } else {
            this.bot.getConnection().getService().cancelRequestInviteIntoParty(id);
        }
    }

    @Override
    public final void handleRequestJoinParty(String name) {
        if (BotService.getInstance().exist(this.bot.getServer().getId(), name, 0)) {
            this.bot.getConnection().getService().acceptPleaseParty(name);
        }
    }

    @Override
    public final String getStatusName() {
        if (!this.bot.isEnable()) {
            return "Ch\u01b0a k\u00edch ho\u1ea1t";
        }
        if (this.bot.getCurrentScreen() == 1) {
            if (this.keepBotOnline) {
                return "\u0110ang gi\u1eef bot online";
            }
            if (this.isChangeMap) {
                return "\u0110ang chuy\u1ec3n \u0111\u1ebfn map " + TileMap.mapNames[this.mapChange] + " (" + this.mapChange + ")";
            }
            if (this.isChangeZone) {
                return "\u0110ang chuy\u1ec3n sang khu " + this.zoneChange;
            }
            return "\u0110ang ho\u1ea1t \u0111\u1ed9ng";
        }
        if (this.bot.getAutoLogin().countdownLogin > 0) {
            return "\u0110\u0103ng nh\u1eadp l\u1ea1i sau " + this.bot.getAutoLogin().countdownLogin + "s";
        }
        if (this.bot.getAutoLogin().isLoginSubmiting) {
            return "\u0110ang \u0111\u0103ng nh\u1eadp";
        }
        return "Ch\u01b0a \u0111\u0103ng nh\u1eadp";
    }

    @Override
    public final void handleServerDialogMessage(String text) {
        super.handleServerDialogMessage(text);
        if (text.equals("Map \u0111\u00e3 \u0111\u1ea7y, vui l\u00f2ng th\u1eed l\u1ea1i sau v\u00e0i ph\u00fat n\u1eefa.")) {
            this.zoneIsFull = true;
        }
    }

    @Override
    public final void handleServerMessage(String text) {
        super.handleServerMessage(text);
        if (text.equals("Khu v\u1ef1c n\u00e0y \u0111\u00e3 \u0111\u1ea7y")) {
            this.zoneIsFull = true;
        }
    }

    private Bot findBotParty(Bot ignore, int serverId, int mapId, int zoneId) {
        List<Bot> botOrders = BotService.getInstance().getBotOrders();
        return botOrders.stream().filter(bo -> bo != ignore && bo.getServer().getId() == serverId && bo.isOnline() && bo.isMapValid() && bo.getTileMap().getMapId() == mapId && bo.getTileMap().getZoneId() == zoneId && (bo.getParty().isEmpty() || bo.isLeaderOfParty() && bo.getParty().size() < 6)).findFirst().orElse(null);
    }

    public void setMapSaveReturnPoint(int mapSaveReturnPoint) {
        this.mapSaveReturnPoint = mapSaveReturnPoint;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public boolean isKeepBotOnline() {
        return this.keepBotOnline;
    }

    public void setLastKeepBotOnline(long lastKeepBotOnline) {
        this.lastKeepBotOnline = lastKeepBotOnline;
    }
}

