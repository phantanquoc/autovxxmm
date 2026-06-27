/*
 * Decompiled with CFR 0.152.
 */
package service.entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Blocker {
    private int serverId;
    private String name;

    public void read(DataInputStream dis) throws Exception {
        this.serverId = dis.readInt();
        this.name = dis.readUTF();
    }

    public void write(DataOutputStream dos) throws Exception {
        dos.writeInt(this.serverId);
        dos.writeUTF(this.name);
    }

    public Blocker(int serverId, String name) {
        this.serverId = serverId;
        this.name = name;
    }

    public Blocker() {
    }

    public int getServerId() {
        return this.serverId;
    }

    public String getName() {
        return this.name;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setName(String name) {
        this.name = name;
    }
}

