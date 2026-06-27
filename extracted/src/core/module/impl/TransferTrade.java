/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.model.Char;
import core.module.Trade;
import core.module.impl.TransferScreen;
import utils.Res;

public class TransferTrade
extends Trade {
    private final TransferScreen screen;

    public TransferTrade(Bot bot, TransferScreen screen) {
        this.bot = bot;
        this.screen = screen;
        this.reset();
    }

    @Override
    public final void updateImpl() {
        if (this.typeTrade == 0) {
            this.lock(this.calculateCoinTransfer());
        } else if (this.typeTrade == 1 && this.typeTradeOrder >= 1) {
            this.accept();
        }
    }

    @Override
    public final void handleInvite(Char chr) {
    }

    @Override
    public final void start(String name) {
        if (this.screen.isOnTransfer() && this.screen.getBotTransfer() != null && this.screen.getBotTransfer().getMyChar().getName().equals(name)) {
            super.start(name);
        } else {
            this.cancelTrade(false);
        }
    }

    @Override
    public final void success() {
        this.bot.debug("B\u01a1m th\u00e0nh c\u00f4ng " + Res.moneyFormat(this.coinTrade) + " xu cho nh\u00e2n v\u1eadt: " + this.tradeName);
    }

    private int calculateCoinTransfer() {
        Bot botTransfer = this.screen.getBotTransfer();
        int coinNeed = (botTransfer.getTypeLuckyDraw() == 0 ? 10000000 : 100000) - botTransfer.getMyChar().getCoin();
        if (coinNeed > this.bot.getMyChar().getCoin()) {
            coinNeed = this.bot.getMyChar().getCoin();
        }
        return coinNeed;
    }
}

