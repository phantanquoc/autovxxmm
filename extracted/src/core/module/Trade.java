/*
 * Decompiled with CFR 0.152.
 */
package core.module;

import core.model.Bot;
import core.model.Char;
import core.model.Item;
import core.model.TradeLog;
import core.module.impl.CollectTrade;
import core.module.impl.OrderTrade;
import utils.Res;

public abstract class Trade {
    protected Bot bot;
    private boolean show;
    public volatile String tradeName;
    public volatile int typeTrade;
    public volatile int typeTradeOrder;
    public int coinTrade;
    public volatile int coinTradeOrder;
    public volatile Item[] arrItemTrade;
    public volatile Item[] arrItemTradeOrder;
    public volatile boolean haveItemTradeOrder;
    public int coinBefore;
    public TradeLog log = null;
    private long lastInviteTrade;
    protected long timeStart;
    public volatile boolean tradeSuccess;

    public final void update() {
        if (!this.tradeSuccess) {
            this.updateImpl();
        }
    }

    public abstract void updateImpl();

    public void cancelTrade(boolean isSpam) {
        this.bot.getConnection().getService().cancelTrade(false);
        this.reset();
    }

    public void success() {
    }

    public void start(String name) {
        this.tradeName = name;
        this.typeTradeOrder = 0;
        this.typeTrade = 0;
        this.coinTradeOrder = 0;
        this.coinTrade = 0;
        this.arrItemTradeOrder = new Item[12];
        this.arrItemTrade = this.arrItemTradeOrder;
        this.log = new TradeLog(this.bot.getServer().getId(), this.bot.getMyChar().getName(), name, this.bot.getMyChar().getCoin());
        this.timeStart = Res.t();
        this.show = true;
    }

    public final void reset() {
        this.beforeReset();
        this.tradeName = null;
        this.haveItemTradeOrder = false;
        this.typeTradeOrder = 0;
        this.typeTrade = 0;
        if (this.arrItemTrade != null) {
            for (int i = 0; i < this.arrItemTrade.length; ++i) {
                if (this.arrItemTrade[i] == null) continue;
                this.bot.getMyChar().getArrItemBag()[this.arrItemTrade[i].indexUI] = this.arrItemTrade[i];
                this.arrItemTrade[i] = null;
            }
        }
        this.coinTradeOrder = 0;
        this.coinTrade = 0;
        this.arrItemTradeOrder = null;
        this.arrItemTrade = null;
        this.log = null;
        this.tradeSuccess = false;
        this.show = false;
        this.timeStart = 0L;
    }

    public void beforeReset() {
    }

    public void addSpam() {
    }

    public void lock(int coinTrade) {
        this.coinTrade = coinTrade;
        this.bot.getConnection().getService().tradeLock(coinTrade, this.arrItemTrade);
        this.typeTrade = 1;
    }

    public void accept() {
        this.coinBefore = this.bot.getMyChar().getCoin();
        this.bot.getConnection().getService().tradeAccept();
        this.typeTrade = 2;
    }

    public abstract void handleInvite(Char var1);

    public void findThenInvite(String name) {
        if (Res.t() - this.lastInviteTrade > 5000L) {
            Char chr = this.bot.getScreen().findCharInMap(name);
            if (chr != null) {
                if (Res.abs(this.bot.getMyChar().getPosX() - chr.getPosX()) > 30 || Res.abs(this.bot.getMyChar().getPosY() - chr.getPosY()) > 30) {
                    this.bot.getScreen().move(chr.getPosX(), chr.getPosY());
                    Res.sleep(500L);
                }
                this.bot.getConnection().getService().tradeInvite(chr.getCharId());
            }
            this.lastInviteTrade = Res.t();
        }
    }

    public OrderTrade getAsOrderTrade() {
        return (OrderTrade)this;
    }

    public CollectTrade getAsCollectTrade() {
        return (CollectTrade)this;
    }

    public boolean isShow() {
        return this.show;
    }

    public void setTradeSuccess(boolean tradeSuccess) {
        this.tradeSuccess = tradeSuccess;
    }
}

