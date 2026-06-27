/*
 * Decompiled with CFR 0.152.
 */
package network;

import java.util.Vector;
import network.Connection;
import network.Message;
import utils.Res;

public class DataSender
implements Runnable {
    private final Connection connection;
    private final Vector<Message> messages;
    public boolean thread;

    public DataSender(Connection connection) {
        this.connection = connection;
        this.messages = new Vector();
    }

    @Override
    public final void run() {
        this.thread = true;
        while (this.thread) {
            try {
                if (this.connection.isUnlocked) {
                    while (!this.messages.isEmpty()) {
                        Message message = this.messages.firstElement();
                        this.connection.doSendMessage(message);
                        this.messages.removeElementAt(0);
                    }
                }
                Res.sleep(10L);
            }
            catch (Exception e) {
                this.connection.closeConnectionAsynchronous();
            }
        }
    }

    public void addMessage(Message message) {
        this.messages.addElement(message);
    }

    public void close() {
        this.messages.removeAllElements();
        this.thread = false;
    }
}

