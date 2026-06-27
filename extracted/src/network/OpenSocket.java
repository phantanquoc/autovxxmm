/*
 * Decompiled with CFR 0.152.
 */
package network;

import lib.mSocket;
import network.Connection;
import network.Message;

public class OpenSocket
implements Runnable {
    private final Connection connection;

    public OpenSocket(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        this.connection.isConnecting = true;
        Thread.currentThread().setPriority(1);
        this.connection.isConnected = true;
        try {
            this.connection.socket = new mSocket(this.connection.bot.getServerIP(), this.connection.bot.getServerPort());
            this.connection.dos = this.connection.socket.getOutputStream();
            this.connection.dis = this.connection.socket.getInputStream();
            this.connection.threadSend = new Thread((Runnable)this.connection.dataSender, "DataSender [" + this.connection.bot.getAccount() + "]");
            this.connection.threadRead = new Thread((Runnable)this.connection.dataReader, "DataReader [" + this.connection.bot.getAccount() + "]");
            this.connection.threadReceive = new Thread((Runnable)this.connection.dataReceiver, "DataReceiver [" + this.connection.bot.getAccount() + "]");
            this.connection.threadSend.start();
            this.connection.threadRead.start();
            this.connection.threadReceive.start();
            Message message = new Message(-27);
            this.connection.doSendMessage(message);
            this.connection.isConnecting = false;
        }
        catch (Exception e) {
            this.connection.bot.getAutoLogin().reconnect();
        }
    }
}

