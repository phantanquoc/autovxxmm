/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.model.Char;
import core.model.Order;
import core.module.Trade;
import core.module.impl.OrderScreen;
import core.module.impl.CollectScreen;
import core.service.MessageStream;
import core.service.Spam;
import main.Application;
import service.OrderService;
import service.SettingService;
import service.TradeLogService;
import utils.Res;

public class OrderTrade
extends Trade {
    public static final int[][] LIMIT_COIN_ORDERS = new int[][]{{1000000, 50000000}, {10000, 100000}};
    private static final int[] LIMIT_COIN_REWARDS = new int[]{10000000, 0};
    private final OrderScreen screen;
    private Bot botCollect;
    private Bot botTransfer;
    private MessageStream.TradeOrder tradeOrder;

    public OrderTrade(Bot bot, OrderScreen screen) {
        this.bot = bot;
        this.screen = screen;
        this.reset();
    }

    @Override
    public void updateImpl() {
        if (this.haveItemTradeOrder) {
            this.bot.getConnection().getService().chatPrivate(this.tradeName, "ch\u1ec9 \u0111\u01b0\u1ee3c giao d\u1ecbch xu!");
            this.cancelTrade(true);
            return;
        }
        if (this.isTradeOrderWin(this.tradeName)) {
            Order order = this.screen.getOrder();
            if (this.typeTrade == 0) {
                this.lock(order.getCoinReward());
            } else if (this.typeTrade == 1 && this.typeTradeOrder == 2) {
                this.accept();
            }
        } else if (this.isTradeOrder(this.tradeName)) {
            if (this.bot.getMessageStream().getTradeTimeRemain(this.tradeOrder) <= 0) {
                this.bot.getConnection().getService().chatPrivate(this.tradeName, "\u0111\u00e3 h\u1ebft th\u1eddi gian giao d\u1ecbch");
                this.cancelTrade(true);
                return;
            }
            if (this.typeTrade == 0) {
                this.lock(0);
            } else if (this.typeTradeOrder >= 1) {
                if (this.coinTradeOrder < LIMIT_COIN_ORDERS[this.tradeOrder.getType()][0] || this.coinTradeOrder > LIMIT_COIN_ORDERS[this.tradeOrder.getType()][1]) {
                    this.bot.getConnection().getService().chatPrivate(this.tradeName, "s\u1ed1 xu \u0111\u1eb7t c\u01b0\u1ee3c ph\u1ea3i t\u1eeb " + Res.moneyFormat(LIMIT_COIN_ORDERS[this.tradeOrder.getType()][0]) + " \u0111\u1ebfn " + Res.moneyFormat(LIMIT_COIN_ORDERS[this.tradeOrder.getType()][1]) + " xu");
                    this.cancelTrade(true);
                } else if (this.typeTrade == 1 && this.typeTradeOrder == 2) {
                    this.accept();
                }
            }
        } else if (this.isTradeCollect(this.tradeName) || this.isTradeManager(this.tradeName)) {
            if (this.typeTrade == 0) {
                int coins = this.calculateCoinToCollect(this.bot.getTypeLuckyDraw(), this.bot.getMyChar().getCoin());
                this.lock(coins);
            } else if (this.typeTrade == 1 && this.typeTradeOrder >= 1) {
                this.accept();
            }
        } else if (this.isTradeTransfer(this.tradeName)) {
            if (this.typeTrade == 0) {
                this.lock(0);
            } else if (this.typeTrade == 1 && this.typeTradeOrder >= 1) {
                this.accept();
            }
        } else {
            this.bot.getConnection().getService().chatPrivate(this.tradeName, "giao d\u1ecbch kh\u00f4ng t\u1ed3n t\u1ea1i!");
            this.cancelTrade(false);
        }
    }

    @Override
    public void handleInvite(Char chr) {
        if (this.isTradeOrderWin(chr.getName())) {
            Order order = this.screen.getOrder();
            if (this.bot.getMyChar().getCoin() - order.getCoinReward() >= LIMIT_COIN_REWARDS[order.getType()]) {
                this.bot.getConnection().getService().acceptInviteTrade(chr.getCharId());
            } else {
                this.bot.getConnection().getService().chatPrivate(chr.getName(), "bot hi\u1ec7n t\u1ea1i kh\u00f4ng \u0111\u1ee7 xu \u0111\u1ec3 tr\u1ea3 th\u01b0\u1edfng, h\u00e3y li\u00ean h\u1ec7 v\u1edbi admin \u0111\u1ec3 \u0111\u01b0\u1ee3c gi\u1ea3i quy\u1ebft!");
                this.bot.getConnection().getService().cancelInviteTrade();
                OrderService.getInstance().error(order.getId(), "Bot kh\u00f4ng \u0111\u1ee7 xu \u0111\u1ec3 tr\u1ea3 th\u01b0\u1edfng");
                this.screen.resetOrder();
            }
        } else if (this.isTradeOrder(chr.getName())) {
            Order order = this.screen.getOrder();
            if (order == null) {
                this.bot.getConnection().getService().acceptInviteTrade(chr.getCharId());
                this.bot.debug("\u0111\u1ed3ng \u00fd giao d\u1ecbch t\u1ea1o \u0111\u01a1n v\u1edbi nh\u00e2n v\u1eadt: " + chr.getName());
            } else {
                this.bot.getConnection().getService().cancelInviteTrade();
                this.bot.getConnection().getService().chatPrivate(chr.getName(), "bot hi\u1ec7n t\u1ea1i \u0111\u00e3 c\u00f3 ng\u01b0\u1eddi ch\u01a1i");
            }
        } else if (this.isTradeCollect(chr.getName())) {
            this.bot.getConnection().getService().acceptInviteTrade(chr.getCharId());
            this.bot.debug("\u0111\u1ed3ng \u00fd giao d\u1ecbch gom xu v\u1edbi nh\u00e2n v\u1eadt: " + chr.getName());
        } else if (this.isTradeTransfer(chr.getName())) {
            this.bot.getConnection().getService().acceptInviteTrade(chr.getCharId());
            this.bot.debug("\u0111\u1ed3ng \u00fd giao d\u1ecbch b\u01a1m xu v\u1edbi nh\u00e2n v\u1eadt: " + chr.getName());
        } else if (this.isTradeManager(chr.getName()) && this.bot.getMyChar().getCoin() > 0) {
            this.bot.getConnection().getService().acceptInviteTrade(chr.getCharId());
            this.bot.debug("\u0111\u1ed3ng \u00fd giao d\u1ecbch gom xu v\u1edbi qu\u1ea3n l\u00fd: " + chr.getName());
        } else {
            this.bot.getConnection().getService().cancelInviteTrade();
        }
    }

    @Override
    public final void start(String name) {
        if (this.isTradeOrderWin(name)) {
            super.start(name);
        } else if (this.isTradeOrder(name)) {
            this.tradeOrder = this.bot.getMessageStream().getTradeOrder(name);
            super.start(name);
        } else if (this.isTradeCollect(name) || this.isTradeTransfer(name) || this.isTradeManager(name)) {
            super.start(name);
        } else {
            this.cancelTrade(false);
        }
    }

    @Override
    public final void addSpam() {
        if (this.isTradeOrder(this.tradeName)) {
            Spam.getInstance(this.bot.getServer().getId()).addSpam(this.tradeName);
            if (Spam.getInstance(this.bot.getServer().getId()).isBlocked(this.tradeName)) {
                this.bot.getConnection().getService().chatPrivate(this.tradeName, "b\u1ea1n \u0111\u00e3 b\u1ecb h\u1ec7 th\u1ed1ng ph\u00e1t hi\u1ec7n spam, \u0111\u01b0\u1ee3c m\u1edf kho\u00e1 sau: " + SettingService.getInstance().getTimeBlockSpam() + " ph\u00fat!");
                this.bot.getMessageStream().removeTradeOrder(this.tradeName);
            }
        }
    }

    @Override
    public final void success() {
        if (this.isTradeOrderWin(this.tradeName)) {
            Order order = this.screen.getOrder();
            Spam.getInstance(this.bot.getServer().getId()).removeCharSpam(order.getName());
            this.bot.getLocalLog().saveOrderRewardLog(this);
            this.bot.getConnection().getService().chatPrivate(order.getName(), "\u0111\u00e3 tr\u1ea3 th\u01b0\u1edfng th\u00e0nh c\u00f4ng " + Res.moneyFormat(order.getCoinReward()) + " xu");
            if (SettingService.getInstance().isEnableMessageReward() && !SettingService.getInstance().getMessageReward().isEmpty()) {
                this.bot.getConnection().getService().chatPrivate(order.getName(), SettingService.getInstance().getMessageReward());
            }
            OrderService.getInstance().reward(order.getId());
            this.log.success(this.bot.getMyChar().getCoin(), "Tr\u1ea3 th\u01b0\u1edfng \u0111\u01a1n #" + order.getId(), 1);
            TradeLogService.save(this.log);
            this.screen.resetOrder();
        } else if (this.isTradeOrder(this.tradeName)) {
            String message;
            String description;
            Spam.getInstance(this.bot.getServer().getId()).removeCharSpam(this.tradeName);
            this.bot.getLocalLog().saveOrderCreateLog(this);
            this.tradeOrder.setCoin(this.coinTradeOrder);
            Order order = OrderService.getInstance().create(this.tradeOrder.getServerId(), this.tradeOrder.getName(), this.bot.getMyChar().getName(), this.tradeOrder.getSecond(), this.tradeOrder.getType(), System.currentTimeMillis(), this.coinTradeOrder, this.bot.getId(), Application.isSplitClient ? Application.client : -1);
            this.bot.getMessageStream().removeTradeOrder(this.tradeName);
            String optionalMessage = null;
            if (order == null) {
                description = "T\u1ea1o \u0111\u01a1n th\u1ea5t b\u1ea1i";
                message = "c\u00f3 l\u1ed7i x\u1ea3y ra trong qu\u00e1 tr\u00ecnh t\u1ea1o \u0111\u01a1n";
                if (SettingService.getInstance().isEnableMessageOrderError()) {
                    optionalMessage = SettingService.getInstance().getMessageOrderError();
                }
            } else {
                description = "T\u1ea1o \u0111\u01a1n #" + order.getId();
                message = "\u0111\u00e3 t\u1ea1o th\u00e0nh c\u00f4ng \u0111\u01a1n #" + order.getId() + " \u0111\u1eb7t c\u01b0\u1ee3c " + Res.moneyFormat(order.getCoinOrder()) + " xu v\u00e0o gi\u00e2y th\u1ee9 " + order.getSecond();
                if (SettingService.getInstance().isEnableMessageOrder()) {
                    optionalMessage = SettingService.getInstance().getMessageOrder();
                }
                this.screen.startOrder(order);
            }
            this.bot.getConnection().getService().chatPrivate(this.tradeName, message);
            if (optionalMessage != null && !optionalMessage.isEmpty()) {
                this.bot.getConnection().getService().chatPrivate(this.tradeName, optionalMessage);
            }
            this.log.success(this.bot.getMyChar().getCoin(), description, 0);
            TradeLogService.save(this.log);
        } else if (this.isTradeCollect(this.tradeName)) {
            this.bot.debug("giao d\u1ecbch th\u00e0nh c\u00f4ng " + Res.moneyFormat(this.coinTradeOrder) + " xu cho nh\u00e2n v\u1eadt: " + this.tradeName);
        } else if (this.isTradeManager(this.tradeName)) {
            this.bot.debug("giao d\u1ecbch th\u00e0nh c\u00f4ng " + Res.moneyFormat(this.coinTradeOrder) + " xu cho qu\u1ea3n l\u00fd: " + this.tradeName);
            this.log.success(this.bot.getMyChar().getCoin(), "Gom xu qua qu\u1ea3n l\u00fd", 2);
            TradeLogService.save(this.log);
        }
    }

    private boolean isTradeOrderWin(String name) {
        return this.screen.getOrder() != null && this.screen.getOrder().hasName(name) && this.screen.getOrder().hasStatus(3);
    }

    private boolean isTradeOrder(String name) {
        return this.tradeOrder != null && this.tradeOrder.getName().equals(name) || this.bot.getMessageStream().existTradeOrder(name);
    }

    private boolean isTradeCollect(String name) {
        return this.botCollect != null && this.botCollect.getMyChar().getName().equals(name);
    }

    private boolean isTradeTransfer(String name) {
        return this.botTransfer != null && this.botTransfer.getMyChar().getName().equals(name);
    }

    private boolean isTradeManager(String name) {
        return this.bot.getManager() != null && this.bot.getManager().equals(name);
    }

    private int calculateCoinToCollect(int typeLucky, int coins) {
        return (int) Math.max(0L, (long) coins - CollectScreen.coinKeep);
    }

    @Override
    public final void beforeReset() {
        this.tradeOrder = null;
    }

    public Bot getBotCollect() {
        return this.botCollect;
    }

    public void setBotCollect(Bot botCollect) {
        this.botCollect = botCollect;
    }

    public Bot getBotTransfer() {
        return this.botTransfer;
    }

    public void setBotTransfer(Bot botTransfer) {
        this.botTransfer = botTransfer;
    }
}

