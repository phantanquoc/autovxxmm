/*
 * Decompiled with CFR 0.152.
 */
package core.model;

public class Server {
    private int id;
    private String name;
    private String ip;
    private int port;
    private byte type;

    public String toString() {
        return this.id + 1 + ". " + this.name;
    }

    public Server(int id, String name, String ip, int port, byte type) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.type = type;
    }

    public Server() {
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public byte getType() {
        return this.type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setType(byte type) {
        this.type = type;
    }
}

