/*
 * Decompiled with CFR 0.152.
 */
package core.model;

public class Order {
    public static final int WAIT = 0;
    public static final int BET = 1;
    public static final int LOSE = 2;
    public static final int WIN = 3;
    public static final int REWARD = 4;
    public static final int ERROR = 5;
    private int id;
    private int serverId;
    private String name;
    private String bot;
    private int second;
    private byte type;
    private long timeStart;
    private long timeStop;
    private int status;
    private int coinOrder;
    private int coinWin;
    private int coinFee;
    private int coinReward;

    public Order(int id, int serverId, String name, String bot, int second, byte type, long timeStart, int coinOrder) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.bot = bot;
        this.second = second;
        this.type = type;
        this.timeStart = timeStart;
        this.coinOrder = coinOrder;
        this.status = 0;
    }

    public boolean hasStatus(int status) {
        return this.status == status;
    }

    public boolean hasStatus(int ... status) {
        for (int s : status) {
            if (this.status != s) continue;
            return true;
        }
        return false;
    }

    public boolean hasName(String name) {
        return this.name.equals(name);
    }

    public Order(int id, int serverId, String name, String bot, int second, byte type, long timeStart, long timeStop, int status, int coinOrder, int coinWin, int coinFee, int coinReward) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.bot = bot;
        this.second = second;
        this.type = type;
        this.timeStart = timeStart;
        this.timeStop = timeStop;
        this.status = status;
        this.coinOrder = coinOrder;
        this.coinWin = coinWin;
        this.coinFee = coinFee;
        this.coinReward = coinReward;
    }

    public Order() {
    }

    public int getId() {
        return this.id;
    }

    public int getServerId() {
        return this.serverId;
    }

    public String getName() {
        return this.name;
    }

    public String getBot() {
        return this.bot;
    }

    public int getSecond() {
        return this.second;
    }

    public byte getType() {
        return this.type;
    }

    public long getTimeStart() {
        return this.timeStart;
    }

    public long getTimeStop() {
        return this.timeStop;
    }

    public int getStatus() {
        return this.status;
    }

    public int getCoinOrder() {
        return this.coinOrder;
    }

    public int getCoinWin() {
        return this.coinWin;
    }

    public int getCoinFee() {
        return this.coinFee;
    }

    public int getCoinReward() {
        return this.coinReward;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBot(String bot) {
        this.bot = bot;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public void setTimeStop(long timeStop) {
        this.timeStop = timeStop;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCoinOrder(int coinOrder) {
        this.coinOrder = coinOrder;
    }

    public void setCoinWin(int coinWin) {
        this.coinWin = coinWin;
    }

    public void setCoinFee(int coinFee) {
        this.coinFee = coinFee;
    }

    public void setCoinReward(int coinReward) {
        this.coinReward = coinReward;
    }
}

