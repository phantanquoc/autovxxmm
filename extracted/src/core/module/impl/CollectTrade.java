/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.model.Char;
import core.module.Trade;
import core.module.impl.CollectScreen;
import utils.Res;

public class CollectTrade
extends Trade {
    private final CollectScreen screen;

    public CollectTrade(Bot bot, CollectScreen screen) {
        this.bot = bot;
        this.screen = screen;
        this.reset();
    }

    @Override
    public void updateImpl() {
        if (!this.screen.isOnCollect()) {
            this.cancelTrade(false);
            return;
        }
        if (this.typeTrade == 0) {
            this.lock(0);
        } else if (this.typeTrade == 1 && this.typeTradeOrder >= 1) {
            this.accept();
        }
    }

    @Override
    public final void start(String name) {
        if (this.screen.isOnCollect() && this.screen.getBotCollect() != null && this.screen.getBotCollect().getMyChar().getName().equals(name)) {
            super.start(name);
        } else {
            this.cancelTrade(false);
        }
    }

    @Override
    public void handleInvite(Char chr) {
    }

    @Override
    public final void success() {
        this.bot.debug("Gom th\u00e0nh c\u00f4ng " + Res.moneyFormat(this.coinTradeOrder) + " xu t\u1eeb nh\u00e2n v\u1eadt: " + this.tradeName);
    }
}

