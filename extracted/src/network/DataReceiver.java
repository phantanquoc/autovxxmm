/*
 * Decompiled with CFR 0.152.
 */
package network;

import java.io.IOException;
import network.Connection;
import network.Message;
import utils.Res;

public class DataReceiver
implements Runnable {
    public Connection connection;
    public boolean thread;

    public DataReceiver(Connection connection) {
        this.connection = connection;
    }

    @Override
    public final void run() {
        this.thread = true;
        try {
            Message msg;
            while (this.thread && this.connection.isConnected && (msg = this.readMessage()) != null) {
                this.processMessage(msg);
                Res.sleep(10L);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            this.connection.closeConnectionAsynchronous();
        }
    }

    private void processMessage(Message msg) {
        try {
            if (msg.command == -27) {
                this.getKey(msg);
            } else {
                this.connection.dataReader.addMessage(msg);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Message readMessage() {
        try {
            int size;
            int i;
            byte cmd = this.connection.dis.readByte();
            if (this.connection.isUnlocked) {
                cmd = this.readKey(cmd);
            }
            if (cmd == -32) {
                cmd = this.connection.dis.readByte();
                if (this.connection.isUnlocked) {
                    cmd = this.readKey(cmd);
                }
                byte[] sizeBytes = new byte[4];
                this.connection.dis.readFully(sizeBytes);
                if (this.connection.isUnlocked) {
                    for (i = 0; i < sizeBytes.length; ++i) {
                        sizeBytes[i] = this.readKey(sizeBytes[i]);
                    }
                }
                size = (sizeBytes[0] & 0xFF) << 24 | (sizeBytes[1] & 0xFF) << 16 | (sizeBytes[2] & 0xFF) << 8 | sizeBytes[3] & 0xFF;
            } else if (this.connection.isUnlocked) {
                byte b1 = this.connection.dis.readByte();
                byte b2 = this.connection.dis.readByte();
                size = (this.readKey(b1) & 0xFF) << 8 | this.readKey(b2) & 0xFF;
            } else {
                size = this.connection.dis.readUnsignedShort();
            }
            byte[] data = new byte[size];
            this.connection.dis.readFully(data);
            if (this.connection.isUnlocked) {
                for (i = 0; i < data.length; ++i) {
                    data[i] = this.readKey(data[i]);
                }
            }
            this.connection.receiveByte += 5 + data.length;
            Message msg = new Message(cmd, data);
            return msg;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private void getKey(Message message) throws IOException {
        int i;
        int keySize = message.reader().readByte();
        this.connection.key = new byte[keySize];
        for (i = 0; i < keySize; ++i) {
            this.connection.key[i] = message.reader().readByte();
        }
        for (i = 0; i < this.connection.key.length - 1; ++i) {
            int n = i + 1;
            this.connection.key[n] = (byte)(this.connection.key[n] ^ this.connection.key[i]);
        }
        this.connection.isUnlocked = true;
    }

    private byte readKey(byte b) {
        byte by = this.connection.curR;
        this.connection.curR = (byte)(by + 1);
        byte i = (byte)(this.connection.key[by] & 0xFF ^ b & 0xFF);
        if (this.connection.curR >= this.connection.key.length) {
            this.connection.curR = (byte)(this.connection.curR % this.connection.key.length);
        }
        return i;
    }

    public void close() {
        this.thread = false;
    }
}

