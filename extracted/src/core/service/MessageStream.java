/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import core.model.Order;
import core.model.TileMap;
import core.module.impl.OrderScreen;
import core.module.impl.OrderTrade;
import core.service.Spam;
import java.util.Collections;
import java.util.List;
import lib.mVector;
import service.BlockService;
import service.SettingService;
import utils.Res;

public class MessageStream
implements Runnable {
    private static final List<String> IGNORE_FILTERS = Collections.singletonList("nsozalo");
    public boolean onThread;
    private Thread thread;
    private final Bot bot;
    private final OrderScreen gameScreen;
    private final OrderTrade trade;
    private final mVector vPrivateMessage;
    private final mVector vTradeOrder;
    private final mVector vLastParty;

    public MessageStream(Bot bot, OrderScreen gameScreen, OrderTrade trade) {
        this.bot = bot;
        this.gameScreen = gameScreen;
        this.trade = trade;
        this.vPrivateMessage = new mVector("vPrivateMessage");
        this.vTradeOrder = new mVector("vTradeOrder");
        this.vLastParty = new mVector("vLastParty");
        this.onThread = true;
        this.thread = new Thread((Runnable)this, "MessageStream [" + bot.getAccount() + "]");
        this.thread.start();
    }

    public void clearAllPrivateMessages() {
        this.vPrivateMessage.clear();
    }

    @Override
    public void run() {
        while (this.onThread) {
            Res.sleep(SettingService.getInstance().getProcessInterval());
            try {
                int i;
                for (i = 0; i < this.vPrivateMessage.size(); ++i) {
                    PrivateMessage privateMessage = (PrivateMessage)this.vPrivateMessage.elementAt(i);
                    this.processPrivateMessage(privateMessage);
                    this.vPrivateMessage.remove(i--);
                }
                for (i = 0; i < this.vTradeOrder.size(); ++i) {
                    TradeOrder trade = (TradeOrder)this.vTradeOrder.elementAt(i);
                    if (this.getTradeTimeRemain(trade) > 0 || trade.getCoin() != 0) continue;
                    this.vTradeOrder.remove(i--);
                }
            }
            catch (Exception exception) {
            }
        }
    }

    public void receivePrivateChat(String name, String message) {
        if (BlockService.getInstance().isContains(this.bot.getServer().getId(), name) || Spam.getInstance(this.bot.getServer().getId()).isBlocked(name) || IGNORE_FILTERS.contains(name) || !this.isValidMessage(message)) {
            return;
        }
        PrivateMessage privateMessage = new PrivateMessage(name, message);
        this.vPrivateMessage.addElement(privateMessage);
    }

    private void processPrivateMessage(PrivateMessage m) {
        if (m.message.equals("pt")) {
            if (this.bot.getParty().size() < 6) {
                if (!this.hasParty(m.name)) {
                    this.bot.getMyChar().addParty(m.name);
                    this.addLastParty(m.name);
                } else {
                    this.bot.getConnection().getService().chatPrivate(m.name, "ch\u1ec9 c\u00f3 th\u1ec3 ti\u1ebfp t\u1ee5c y\u00eau c\u1ea7u sau 15 gi\u00e2y");
                }
            } else {
                this.bot.getConnection().getService().chatPrivate(m.name, "nh\u00f3m \u0111\u00e3 \u0111\u1ea1t t\u1ed1i \u0111a th\u00e0nh vi\u00ean");
            }
            return;
        }
        Order order = this.gameScreen.getOrder();
        if (order != null && order.hasName(m.name)) {
            if (order.hasStatus(3)) {
                this.bot.getConnection().getService().chatPrivate(m.name, "h\u00e3y giao d\u1ecbch \u0111\u1ec3 nh\u1eadn th\u01b0\u1edfng th\u1eafng cu\u1ed9c " + Res.moneyFormat(order.getCoinReward()) + " xu");
            } else {
                this.bot.getConnection().getService().chatPrivate(m.name, "b\u1ea1n \u0111\u00e3 tham gia \u0111\u1eb7t c\u01b0\u1ee3c " + Res.moneyFormat(order.getCoinOrder()) + " xu v\u00e0o gi\u00e2y th\u1ee9 " + order.getSecond() + " r\u1ed3i!");
            }
            return;
        }
        int second = this.getNumber(m.message);
        if (second >= 10 && second <= 120) {
            if (order != null) {
                this.bot.getConnection().getService().chatPrivate(m.name, "bot hi\u1ec7n t\u1ea1i \u0111\u00e3 c\u00f3 ng\u01b0\u1eddi ch\u01a1i");
            } else if (this.bot.getScreen().findCharInMap(m.name) == null) {
                this.bot.getConnection().getService().chatPrivate(m.name, "h\u00e3y \u0111\u1ebfn map " + this.bot.getTileMap().getMapName() + ", khu " + this.bot.getTileMap().getZoneId() + " \u0111\u1ec3 \u0111\u1eb7t c\u01b0\u1ee3c");
            } else {
                TradeOrder trade = this.getTradeOrder(m.name);
                if (trade == null) {
                    trade = new TradeOrder(this.bot.getServer().getId(), m.name, this.bot.getTypeLuckyDraw(), second);
                    this.addTradeOrder(trade);
                    this.bot.getConnection().getService().chatPrivate(m.name, "b\u1ea1n \u0111\u00e3 t\u1ea1o \u0111\u01a1n \u0111\u1eb7t c\u01b0\u1ee3c v\u00e0o gi\u00e2y " + second + ", c\u00f3 t\u1ed1i \u0111a " + SettingService.getInstance().getTimeCreateOrder() + " gi\u00e2y \u0111\u1ec3 giao d\u1ecbch xu");
                } else {
                    trade.setSecond(second);
                    this.bot.getConnection().getService().chatPrivate(m.name, "\u0111\u00e3 thay \u0111\u1ed5i s\u1ed1 gi\u00e2y \u0111\u1eb7t c\u01b0\u1ee3c th\u00e0nh " + second + ", b\u1ea1n c\u00f2n " + this.getTradeTimeRemain(trade) + " gi\u00e2y \u0111\u1ec3 giao d\u1ecbch xu");
                }
            }
        } else if (second == -1) {
            this.sendSMS(m.name);
        } else {
            this.bot.getConnection().getService().chatPrivate(m.name, "s\u1ed1 gi\u00e2y \u0111\u1eb7t c\u01b0\u1ee3c ph\u1ea3i t\u1eeb 10-120 gi\u00e2y!");
        }
    }

    private void sendSMS(String name) {
        if (!this.bot.isMapValid()) {
            return;
        }
        this.bot.getConnection().getService().chatPrivate(name, "Bot \u0111\u1eb7t thu\u00ea VXMM " + (this.bot.getTypeLuckyDraw() == 0 ? "Vip" : "Th\u01b0\u1eddng"));
        this.bot.getConnection().getService().chatPrivate(name, "Bot \u0111ang \u0111\u1ee9ng t\u1ea1i map " + TileMap.mapNames[this.bot.getMapId()] + " khu " + this.bot.getZoneId());
        if (this.bot.getSms() != null) {
            for (String m : this.bot.getSms()) {
                this.bot.getConnection().getService().chatPrivate(name, m);
            }
        }
        this.bot.getConnection().getService().chatPrivate(name, "chat s\u1ed1 gi\u00e2y v\u00e0 giao d\u1ecbch xu \u0111\u1ec3 ti\u1ebfn h\u00e0nh \u0111\u1eb7t c\u01b0\u1ee3c!");
    }

    private int getNumber(String text) {
        try {
            return Integer.parseInt(text);
        }
        catch (Exception e) {
            return -1;
        }
    }

    public TradeOrder getTradeOrder(String name) {
        TradeOrder result = null;
        for (int i = 0; i < this.vTradeOrder.size(); ++i) {
            TradeOrder trade = (TradeOrder)this.vTradeOrder.elementAt(i);
            if (!trade.getName().equals(name)) continue;
            result = trade;
            break;
        }
        return result;
    }

    public boolean existTradeOrder(String name) {
        boolean exist = false;
        for (int i = 0; i < this.vTradeOrder.size(); ++i) {
            TradeOrder trade = (TradeOrder)this.vTradeOrder.get(i);
            if (!trade.getName().equals(name)) continue;
            exist = true;
            break;
        }
        return exist;
    }

    private void addTradeOrder(TradeOrder trade) {
        this.vTradeOrder.add(trade);
    }

    public void removeTradeOrder(String name) {
        for (int i = 0; i < this.vTradeOrder.size(); ++i) {
            TradeOrder trade = (TradeOrder)this.vTradeOrder.get(i);
            if (!trade.getName().equals(name)) continue;
            this.vTradeOrder.remove(i);
            break;
        }
    }

    public int getTradeTimeRemain(TradeOrder trade) {
        long time = (long)SettingService.getInstance().getTimeCreateOrder() - (Res.t() - trade.getTime()) / 1000L;
        return time < 0L ? 0 : (int)time;
    }

    private boolean isValidMessage(String message) {
        return message.length() <= 5;
    }

    private boolean hasParty(String name) {
        LastParty result = null;
        for (int i = 0; i < this.vLastParty.size(); ++i) {
            LastParty lastParty = (LastParty)this.vLastParty.elementAt(i);
            if (!lastParty.name.equals(name)) continue;
            result = lastParty;
            break;
        }
        if (result == null) {
            return false;
        }
        if (result.isExpired()) {
            this.vLastParty.remove(result);
            return false;
        }
        return true;
    }

    public void addLastParty(String name) {
        this.vLastParty.add(new LastParty(name));
    }

    public void removeLastParty(String name) {
        for (int i = 0; i < this.vLastParty.size(); ++i) {
            LastParty lastParty = (LastParty)this.vLastParty.elementAt(i);
            if (!lastParty.name.equals(name)) continue;
            this.vLastParty.remove(i);
            break;
        }
    }

    public void clearLastParty() {
        this.vLastParty.clear();
    }

    private static class LastParty {
        private String name;
        private long time;

        public LastParty(String name) {
            this.name = name;
            this.time = Res.t();
        }

        private boolean isExpired() {
            return Res.t() - this.time > 15000L;
        }
    }

    public static final class TradeOrder {
        private int serverId;
        private String name;
        private int second;
        private byte type;
        private int coin;
        private long time;

        private TradeOrder(int serverId, String name, byte type, int second) {
            this.serverId = serverId;
            this.name = name;
            this.type = type;
            this.second = second;
            this.time = Res.t();
        }

        public TradeOrder() {
        }

        public int getServerId() {
            return this.serverId;
        }

        public String getName() {
            return this.name;
        }

        public int getSecond() {
            return this.second;
        }

        public byte getType() {
            return this.type;
        }

        public int getCoin() {
            return this.coin;
        }

        public long getTime() {
            return this.time;
        }

        public void setServerId(int serverId) {
            this.serverId = serverId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public void setType(byte type) {
            this.type = type;
        }

        public void setCoin(int coin) {
            this.coin = coin;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }

    private static class PrivateMessage {
        private String name;
        private String message;

        public PrivateMessage(String name, String message) {
            this.name = name;
            this.message = message;
        }
    }
}

