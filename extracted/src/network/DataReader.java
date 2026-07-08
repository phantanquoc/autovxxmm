/*
 * Decompiled with CFR 0.152.
 */
package network;

import java.util.Vector;
import network.Connection;
import network.Message;
import utils.Res;

public class DataReader
implements Runnable {
    private final Connection connection;
    private final Vector<Message> vMessage;
    private volatile boolean onThread;

    public DataReader(Connection connection) {
        this.connection = connection;
        this.vMessage = new Vector();
    }

    public void addMessage(Message msg) {
        this.vMessage.addElement(msg);
    }

    @Override
    public void run() {
        this.onThread = true;
        while (this.onThread && this.connection.isConnected) {
            try {
                if (this.connection.isUnlocked && !this.vMessage.isEmpty()) {
                    while (!this.vMessage.isEmpty()) {
                        Message msg = this.vMessage.firstElement();
                        try {
                            this.connection.getController().onMessage(msg);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            this.vMessage.removeElementAt(0);
                        }
                    }
                }
                Res.sleep(10L);
            }
            catch (Exception exception) {}
        }
    }

    public void close() {
        this.onThread = false;
        this.vMessage.removeAllElements();
    }
}

