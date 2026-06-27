/*
 * Decompiled with CFR 0.152.
 */
package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
    public byte command;
    private ByteArrayOutputStream os = null;
    private DataOutputStream dos = null;
    private ByteArrayInputStream is = null;
    private DataInputStream dis = null;

    public Message() {
    }

    public Message(byte command) {
        this.command = command;
        this.os = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.os);
    }

    public Message(byte command, byte[] data) {
        this.command = command;
        this.is = new ByteArrayInputStream(data);
        this.dis = new DataInputStream(this.is);
    }

    public final byte[] getData() {
        return this.os.toByteArray();
    }

    public final DataInputStream reader() {
        return this.dis;
    }

    public final DataOutputStream writer() {
        return this.dos;
    }

    public final void cleanup() {
        try {
            if (this.dis != null) {
                this.dis.close();
                this.dis = null;
            }
            if (this.dos != null) {
                this.dos.close();
                this.dos = null;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

