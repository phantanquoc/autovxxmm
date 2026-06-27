/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import utils.Res;

public class TradeLog {
    public static final int TYPE_NHAN_XU = 0;
    public static final int TYPE_TRA_THUONG = 1;
    public static final int TYPE_GOM_XU = 2;
    private int serverId;
    private String name;
    private String customer;
    private int before;
    private int after;
    private int change;
    private String description;
    private int type;
    private long timeStart;
    private long timeStop;

    public TradeLog(int serverId, String name, String customer, int before) {
        this.serverId = serverId;
        this.name = name;
        this.customer = customer;
        this.before = before;
        this.timeStart = Res.t();
    }

    public void success(int after, String description, int type) {
        this.after = after;
        this.description = description;
        this.type = type;
        this.change = this.after - this.before;
        this.timeStop = Res.t();
    }

    public int getServerId() {
        return this.serverId;
    }

    public String getName() {
        return this.name;
    }

    public String getCustomer() {
        return this.customer;
    }

    public int getBefore() {
        return this.before;
    }

    public int getAfter() {
        return this.after;
    }

    public int getChange() {
        return this.change;
    }

    public String getDescription() {
        return this.description;
    }

    public int getType() {
        return this.type;
    }

    public long getTimeStart() {
        return this.timeStart;
    }

    public long getTimeStop() {
        return this.timeStop;
    }
}

