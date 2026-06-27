/*
 * Decompiled with CFR 0.152.
 */
package utils;

import core.model.Bot;
import core.model.Char;
import core.model.TileMap;
import core.module.Trade;
import java.util.Calendar;
import utils.FileUtils;
import utils.Res;

public class LocalLog {
    private Bot bot;

    public LocalLog(Bot bot) {
        this.bot = bot;
    }

    public String prefix() {
        return this.bot.getServer().getId() + "-" + this.bot.getAccount();
    }

    public void saveOrderCreateLog(Trade trade) {
        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(1);
            int month = c.get(2) + 1;
            int day = c.get(5);
            String file = "order-logs/" + this.prefix() + "/log-" + year + "-" + Res.addZero(month) + "-" + Res.addZero(day) + ".txt";
            String text = "===== LOG NH\u1eacN XU \u0110\u1eb6T THU\u00ca =====";
            text = text + "\r\nTh\u1eddi gian: " + Res.currentDayTime();
            text = text + "\r\n\u0110\u1ecba \u0111i\u1ec3m: " + TileMap.mapNames[this.bot.getTileMap().getMapId()] + ", khu v\u1ef1c: " + this.bot.getTileMap().getZoneId();
            text = text + "\r\nNh\u00e2n v\u1eadt nh\u1eadn xu: " + this.bot.getMyChar().getName();
            text = text + "\r\nNh\u00e2n v\u1eadt g\u1eedi xu: " + trade.tradeName;
            text = text + "\r\nS\u1ed1 xu nh\u1eadn \u0111\u01b0\u1ee3c: " + Res.moneyFormat(trade.coinTradeOrder);
            text = text + "\r\nS\u1ed1 xu ban \u0111\u1ea7u: " + Res.moneyFormat(trade.coinBefore);
            text = text + "\r\nS\u1ed1 xu sau giao d\u1ecbch: " + Res.moneyFormat(this.bot.getMyChar().getCoin());
            text = text + "\r\nBi\u00ea\u0301n \u0111\u00f4\u0323ng: t\u0103ng " + Res.moneyFormat(trade.coinTradeOrder) + " xu";
            text = text + "\r\n==========================================\r\n";
            FileUtils.saveString(file, text, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOrderRewardLog(Trade trade) {
        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(1);
            int month = c.get(2) + 1;
            int day = c.get(5);
            String file = "reward-logs/" + this.prefix() + "/log-" + year + "-" + Res.addZero(month) + "-" + Res.addZero(day) + ".txt";
            String text = "===== LOG TR\u1ea2 TH\u01af\u1edeNG =====";
            text = text + "\r\nTh\u1eddi gian: " + Res.currentDayTime();
            text = text + "\r\n\u0110\u1ecba \u0111i\u1ec3m: " + TileMap.mapNames[this.bot.getTileMap().getMapId()] + ", khu v\u1ef1c: " + this.bot.getTileMap().getZoneId();
            text = text + "\r\nNh\u00e2n v\u1eadt nh\u1eadn xu: " + trade.tradeName;
            text = text + "\r\nNh\u00e2n v\u1eadt g\u1eedi xu: " + this.bot.getMyChar().getName();
            text = text + "\r\nS\u1ed1 xu nh\u1eadn \u0111\u01b0\u1ee3c: " + trade.coinTrade;
            text = text + "\r\nS\u1ed1 xu ban \u0111\u1ea7u: " + Res.moneyFormat(trade.coinBefore);
            text = text + "\r\nS\u1ed1 xu sau giao d\u1ecbch: " + Res.moneyFormat(this.bot.getMyChar().getCoin());
            text = text + "\r\nBi\u00ea\u0301n \u0111\u00f4\u0323ng: gi\u1ea3m " + Res.moneyFormat(trade.coinTrade) + " xu";
            text = text + "\r\n==========================================\r\n";
            FileUtils.saveString(file, text, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveCharJoinMap(Char chr) {
        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(1);
            int month = c.get(2) + 1;
            int day = c.get(5);
            String file = "player-logs/" + this.prefix() + "/log-" + year + "-" + Res.addZero(month) + "-" + Res.addZero(day) + ".txt";
            String text = "===== TH\u00d4NG TIN NH\u00c2N V\u1eacT V\u00c0O MAP =====";
            text = text + "\r\nTh\u1eddi gian: " + Res.currentDayTime();
            text = text + "\r\nMap: " + TileMap.mapNames[this.bot.getTileMap().getMapId()] + ", ID: " + this.bot.getTileMap().getMapId() + ", Khu v\u1ef1c: " + this.bot.getTileMap().getZoneId();
            text = text + "\r\nNh\u00e2n v\u1eadt v\u00e0o map: " + chr.getName();
            text = text + "\r\nC\u1ea5p \u0111\u1ed9: " + chr.getLevel();
            text = text + "\r\n==========================================\r\n";
            FileUtils.saveString(file, text, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void totalCoinBet(int coin) {
        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(1);
            int month = c.get(2) + 1;
            String file = "bet-logs/" + this.bot.getNameServer() + "/log-" + year + "-" + Res.addZero(month) + ".txt";
            String curr = FileUtils.readString(file);
            int ncoin = 0;
            try {
                ncoin = Integer.parseInt(curr);
            }
            catch (Exception exception) {
                // empty catch block
            }
            FileUtils.saveString(file, "" + (coin += ncoin), false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

