/*
 * Decompiled with CFR 0.152.
 */
package network;

import core.model.Bot;
import core.module.GameScreen;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lib.mSocket;
import network.Controller;
import network.DataReader;
import network.DataReceiver;
import network.DataSender;
import network.Message;
import network.OpenSocket;
import network.Service;
import service.BotService;
import utils.Res;

public class Connection {
    public final Bot bot;
    public OpenSocket openSocket;
    public mSocket socket;
    public DataSender dataSender;
    public DataReader dataReader;
    public DataReceiver dataReceiver;
    private Controller controller;
    private Service service;
    public DataInputStream dis;
    public DataOutputStream dos;
    public volatile boolean isConnected;
    public volatile boolean isConnecting;
    public volatile boolean isUnlocked;
    public Thread threadSend;
    public Thread threadRead;
    public Thread threadReceive;
    public int sendByte;
    public int receiveByte;
    public byte[] key = null;
    public byte curR;
    public byte curW;

    public Connection(Bot account) {
        this.bot = account;
        this.openSocket = new OpenSocket(this);
        this.dataSender = new DataSender(this);
        this.dataReader = new DataReader(this);
        this.dataReceiver = new DataReceiver(this);
        this.controller = new Controller(this);
        this.service = new Service(this);
    }

    public void openConnect() {
        if (!this.isConnected && !this.isConnecting) {
            this.isUnlocked = false;
            new Thread((Runnable)this.openSocket, "OpenSocket [" + this.bot.getAccount() + "]").start();
        }
    }

    public void closeConnectionAsynchronous() {
        new Thread(() -> {
            this.cleanNetwork();
            this.bot.getAutoLogin().startReconnect(5);
        }).start();
    }

    public void cleanNetwork() {
        this.key = null;
        this.curR = 0;
        this.curW = 0;
        if (this.bot.getCurrentScreen() == 1 && this.bot.getRole() == 0) {
            BotService.getInstance().plusNumActiveOrderBot(-1);
        }
        this.bot.setCurrentScreen(0);
        this.bot.getScreen().onOfflineEvent();
        try {
            this.isConnected = false;
            this.isConnecting = false;
            if (this.dataReceiver != null) {
                this.dataReceiver.close();
            }
            if (this.dataSender != null) {
                this.dataSender.close();
            }
            if (this.dataReader != null) {
                this.dataReader.close();
            }
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
            if (this.dos != null) {
                this.dos.close();
                this.dos = null;
            }
            if (this.dis != null) {
                this.dis.close();
                this.dis = null;
            }
            if (this.bot.getMyChar() != null) {
                this.bot.getMyChar().reset();
            }
            if (this.bot.getTileMap() != null) {
                this.bot.getTileMap().reset();
            }
        }
        catch (Exception e) {
            System.out.println("Clean network fail!");
        }
    }

    public synchronized void doSendMessage(Message m) throws IOException {
        byte[] data = m.getData();
        if (this.isUnlocked) {
            byte b = this.writeKey(m.command);
            this.dos.writeByte(b);
        } else {
            this.dos.writeByte(m.command);
        }
        int size = data.length;
        if (m.command == -31) {
            this.dos.writeShort(size);
        } else if (this.isUnlocked) {
            byte byte1 = this.writeKey((byte)(size >> 8));
            this.dos.writeByte(byte1);
            byte byte2 = this.writeKey((byte)(size & 0xFF));
            this.dos.writeByte(byte2);
        } else {
            this.dos.writeShort(size);
        }
        if (this.isUnlocked) {
            for (int i = 0; i < data.length; ++i) {
                data[i] = this.writeKey(data[i]);
            }
        }
        this.dos.write(data);
        this.sendByte += 5 + data.length;
        this.dos.flush();
    }

    public void sendMessage(Message msg) {
        this.dataSender.addMessage(msg);
    }

    private byte writeKey(byte b) {
        byte by = this.curW;
        this.curW = (byte)(by + 1);
        byte i = (byte)(this.key[by] & 0xFF ^ b & 0xFF);
        if (this.curW >= this.key.length) {
            this.curW = (byte)(this.curW % this.key.length);
        }
        return i;
    }

    public boolean checkout() {
        if (this.bot == null) {
            return true;
        }
        return this.bot.getConnection() == null;
    }

    public void debug(String str) {
        System.out.println(Res.cTime() + " " + this.bot.getAccount().toUpperCase() + ": " + str);
    }

    public GameScreen getGameScreen() {
        return this.bot.getScreen();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return this.controller;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Service getService() {
        return this.service;
    }
}

