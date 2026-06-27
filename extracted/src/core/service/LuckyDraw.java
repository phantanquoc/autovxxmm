/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import utils.Res;

public class LuckyDraw {
    private static final short BET_ACTION = 100;
    private static final short REFRESH_ACTION = 101;
    private static final short OPEN_ACTION = 102;
    private final Bot bot;
    private final Object lock;
    public boolean isShow;
    public short secondCountdown = (short)-1;
    public short numPlayer;
    public String percentWin;
    public String totalCoinJoin;
    public long timeUpdate;
    public int myCoinJoin;
    public String winName;

    public LuckyDraw(Bot bot) {
        this.bot = bot;
        this.lock = new Object();
    }

    public void show(String title, short second_countdown, String total_coin_join, short percent_win_1, String percent_win_2, short num_player, String winnerInfo, String my_coin_join, byte typeLucky) {
        String winNameTag;
        this.totalCoinJoin = total_coin_join;
        this.percentWin = percent_win_1 + "." + percent_win_2 + "%";
        this.numPlayer = num_player;
        this.myCoinJoin = Res.getNumber(my_coin_join);
        this.secondCountdown = second_countdown;
        this.timeUpdate = Res.t();
        String string = winNameTag = winnerInfo.contains("Last winner: ") ? "Last winner: " : "Ng\u01b0\u1eddi v\u1eeba chi\u1ebfn th\u1eafng: ";
        if (winnerInfo.contains(winNameTag)) {
            String[] line;
            for (String s : line = winnerInfo.contains("\r\n") ? winnerInfo.split("\r\n") : winnerInfo.split("\n")) {
                if (!s.contains(winNameTag)) continue;
                this.winName = s.substring(winNameTag.length());
            }
        }
        this.isShow = true;
        this.notifyResponse();
    }

    public boolean open() {
        this.bot.getConnection().getService().luckyDraw((short)102, "", this.bot.getTypeLuckyDraw());
        if (!this.waitResponse()) {
            return false;
        }
        return this.isShow;
    }

    public boolean refresh() {
        this.bot.getConnection().getService().luckyDraw((short)101, "", this.bot.getTypeLuckyDraw());
        return this.waitResponse();
    }

    public void bet(int coin) {
        this.bot.getConnection().getService().luckyDraw((short)100, String.valueOf(coin), this.bot.getTypeLuckyDraw());
        this.waitResponse();
    }

    public void close() {
        this.isShow = false;
    }

    public int curSecond() {
        return (int)((this.timeUpdate + (long)(this.secondCountdown * 1000) - Res.t()) / 1000L);
    }

    public boolean isNewTurn() {
        return this.isShow && this.secondCountdown > 10 && this.myCoinJoin == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean waitResponse() {
        long time = 5000L;
        Object object = this.lock;
        synchronized (object) {
            try {
                long start = Res.t();
                this.lock.wait(5000L);
                return Res.t() - start < 5000L;
            }
            catch (Exception e) {
                return false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void notifyResponse() {
        Object object = this.lock;
        synchronized (object) {
            this.lock.notifyAll();
        }
    }
}

