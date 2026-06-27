/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package core.model;

import com.google.gson.JsonObject;
import core.model.Bot;
import core.model.BotStatus;
import java.util.Objects;
import service.BotService;

public class BotObserver {
    private Bot bot;
    private String name = "";
    private int level;
    private String clan;
    private int coin;
    private int gold;
    private BotStatus status;
    private long lastOnline;

    public BotObserver(Bot bot) {
        this.bot = bot;
        this.status = BotStatus.OFFLINE;
    }

    public void observe() {
        boolean hasChange = false;
        if (!Objects.equals(this.name, this.bot.getMyChar().getName())) {
            this.name = this.bot.getMyChar().getName();
            hasChange = true;
        }
        if (this.level != this.bot.getMyChar().getLevel()) {
            this.level = this.bot.getMyChar().getLevel();
            hasChange = true;
        }
        if (!Objects.equals(this.clan, this.bot.getMyChar().getClanName())) {
            this.clan = this.bot.getMyChar().getClanName();
            hasChange = true;
        }
        if (this.coin != this.bot.getMyChar().getCoin()) {
            this.coin = this.bot.getMyChar().getCoin();
            hasChange = true;
        }
        if (this.gold != this.bot.getMyChar().getGold()) {
            this.gold = this.bot.getMyChar().getGold();
            hasChange = true;
        }
        if (this.status != this.bot.getStatus()) {
            this.status = this.bot.getStatus();
            hasChange = true;
        }
        if (this.lastOnline != this.bot.getLastOnline()) {
            this.lastOnline = this.bot.getLastOnline();
            hasChange = true;
        }
        if (hasChange) {
            ObserveResult observeResult = this.toObserveResult();
            BotService.getInstance().getUpdaterService().addObserveResult(observeResult);
        }
    }

    private ObserveResult toObserveResult() {
        ObserveResult result = new ObserveResult();
        result.id = this.bot.getId();
        result.name = this.name;
        result.level = this.level;
        result.clan = this.clan;
        result.coin = this.coin;
        result.gold = this.gold;
        result.status = this.status;
        result.lastOnline = this.lastOnline;
        return result;
    }

    public Bot getBot() {
        return this.bot;
    }

    public String getName() {
        return this.name;
    }

    public int getLevel() {
        return this.level;
    }

    public String getClan() {
        return this.clan;
    }

    public int getCoin() {
        return this.coin;
    }

    public int getGold() {
        return this.gold;
    }

    public BotStatus getStatus() {
        return this.status;
    }

    public long getLastOnline() {
        return this.lastOnline;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setClan(String clan) {
        this.clan = clan;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setStatus(BotStatus status) {
        this.status = status;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public static class ObserveResult {
        private int id;
        private String name;
        private int level;
        private String clan;
        private int coin;
        private int gold;
        private BotStatus status;
        private long lastOnline;

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", (Number)this.id);
            json.addProperty("name", this.name);
            json.addProperty("level", (Number)this.level);
            json.addProperty("clan", this.clan);
            json.addProperty("coin", (Number)this.coin);
            json.addProperty("gold", (Number)this.gold);
            json.addProperty("status", this.status.name());
            json.addProperty("lastOnline", (Number)this.lastOnline);
            return json;
        }

        public ObserveResult(int id, String name, int level, String clan, int coin, int gold, BotStatus status, long lastOnline) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.clan = clan;
            this.coin = coin;
            this.gold = gold;
            this.status = status;
            this.lastOnline = lastOnline;
        }

        public ObserveResult() {
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public int getLevel() {
            return this.level;
        }

        public String getClan() {
            return this.clan;
        }

        public int getCoin() {
            return this.coin;
        }

        public int getGold() {
            return this.gold;
        }

        public BotStatus getStatus() {
            return this.status;
        }

        public long getLastOnline() {
            return this.lastOnline;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setClan(String clan) {
            this.clan = clan;
        }

        public void setCoin(int coin) {
            this.coin = coin;
        }

        public void setGold(int gold) {
            this.gold = gold;
        }

        public void setStatus(BotStatus status) {
            this.status = status;
        }

        public void setLastOnline(long lastOnline) {
            this.lastOnline = lastOnline;
        }

        public String toString() {
            return "BotObserver.ObserveResult(id=" + this.getId() + ", name=" + this.getName() + ", level=" + this.getLevel() + ", clan=" + this.getClan() + ", coin=" + this.getCoin() + ", gold=" + this.getGold() + ", status=" + (Object)((Object)this.getStatus()) + ", lastOnline=" + this.getLastOnline() + ")";
        }
    }
}

