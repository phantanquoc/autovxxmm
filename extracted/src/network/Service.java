/*
 * Decompiled with CFR 0.152.
 */
package network;

import core.model.Item;
import java.io.IOException;
import network.Connection;
import network.Message;
import utils.Res;

public class Service {
    public Connection connection;

    public Service(Connection connection) {
        this.connection = connection;
    }

    public Message messageNotLogin(byte command) throws IOException {
        Message m = new Message(-29);
        m.writer().writeByte(command);
        return m;
    }

    public Message messageNotMap(byte command) throws IOException {
        Message m = new Message(-28);
        m.writer().writeByte(command);
        return m;
    }

    public static Message messageSubCommand(byte command) throws IOException {
        Message m = new Message(-30);
        m.writer().writeByte(command);
        return m;
    }

    public void setClientType() {
        try {
            Message m = this.messageNotLogin((byte)-125);
            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeBoolean(false);
            m.writer().writeInt(240);
            m.writer().writeInt(320);
            m.writer().writeBoolean(false);
            m.writer().writeBoolean(true);
            m.writer().writeUTF("neovim");
            m.writer().writeByte(0);
            m.writer().writeInt(0);
            m.writer().writeByte(0);
            m.writer().writeInt(0);
            m.writer().writeUTF("");
            this.connection.sendMessage(m);
            m.cleanup();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String pass, byte type) {
        this.setClientType();
        try {
            Message message = this.messageNotLogin((byte)-127);
            message.writer().writeUTF(username);
            message.writer().writeUTF(pass);
            message.writer().writeUTF("1.8.8");
            message.writer().writeUTF("");
            message.writer().writeUTF("");
            message.writer().writeUTF(Res.randomNumberlist());
            message.writer().writeByte(type);
            this.connection.sendMessage(message);
            message.cleanup();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateData() {
        Message m = null;
        try {
            m = this.messageNotMap((byte)-122);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void updateMap() {
        Message m = null;
        try {
            m = this.messageNotMap((byte)-121);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void updateSkill() {
        Message m = null;
        try {
            m = this.messageNotMap((byte)-120);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void updateItem() {
        Message m = null;
        try {
            m = this.messageNotMap((byte)-119);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void clientOk() {
        Message m = null;
        try {
            m = this.messageNotMap((byte)-101);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void selectCharToPlay(String charname) {
        Message m = new Message(-28);
        try {
            m.writer().writeByte(-126);
            m.writer().writeUTF(charname);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.connection.sendMessage(m);
    }

    public void charMove(int xSend, int ySend) {
        try {
            Message m = new Message(1);
            m.writer().writeShort((short)xSend);
            m.writer().writeShort((short)ySend);
            this.connection.sendMessage(m);
            m.cleanup();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void requestChangeMap() {
        Message m = new Message(-17);
        try {
            this.connection.sendMessage(m);
            m.cleanup();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestChangeZone(int zoneId, int itemChange) {
        Message m = new Message(28);
        try {
            m.writer().writeByte(zoneId);
            m.writer().writeByte(itemChange);
            this.connection.sendMessage(m);
            m.cleanup();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void menu(int npcId, int menuId, int optionId) {
        Message m = null;
        try {
            m = new Message(29);
            m.writer().writeByte(npcId);
            m.writer().writeByte(menuId);
            m.writer().writeByte(optionId);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void returnTownFromDead() {
        Message m = null;
        try {
            m = new Message(-9);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tradeInvite(int charId) {
        Message m = null;
        try {
            m = new Message(43);
            m.writer().writeInt(charId);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void acceptInviteTrade(int playerMapId) {
        Message m = null;
        try {
            m = new Message(44);
            m.writer().writeInt(playerMapId);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void cancelInviteTrade() {
        Message m = null;
        try {
            m = new Message(56);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancelTrade(boolean isSpam) {
        if (isSpam) {
            this.connection.bot.getTrade().addSpam();
        }
        Message m = null;
        try {
            m = new Message(57);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void tradeAccept() {
        this.connection.bot.getTrade().coinBefore = this.connection.bot.getMyChar().getCoin();
        Message m = null;
        try {
            m = new Message(46);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tradeLock(int coin, Item[] items) {
        Message m = null;
        try {
            int i;
            m = new Message(45);
            m.writer().writeInt(coin);
            int size = 0;
            for (i = 0; i < items.length; ++i) {
                if (items[i] == null) continue;
                ++size;
            }
            m.writer().writeByte(size);
            for (i = 0; i < items.length; ++i) {
                if (items[i] == null) continue;
                m.writer().writeByte(items[i].indexUI);
            }
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void addFriend(String name) {
        try {
            Message m = new Message(59);
            m.writer().writeUTF(name);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void chatPrivate(String to, String text) {
        if (text.contains("nsozalo")) {
            text = text.replace("nsozalo", "nso****");
        }
        text = "@" + Res.random(100, 999) + ": " + text;
        Message m = null;
        try {
            m = new Message(-22);
            m.writer().writeUTF(to);
            m.writer().writeUTF(text);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void chat(String text) {
        text = "@" + Res.random(10, 99) + ": " + text;
        Message m = null;
        try {
            m = new Message(-23);
            m.writer().writeUTF(text);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void send_Captcha(byte type) {
        Message m = null;
        try {
            m = this.messageNotMap((byte)122);
            m.writer().writeByte(4);
            m.writer().writeByte(type);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void luckyDraw(short type, String money, byte typeLucky) {
        try {
            Message message = new Message(92);
            message.writer().writeShort(type);
            message.writer().writeUTF(money);
            message.writer().writeByte(typeLucky);
            this.connection.sendMessage(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectSkill(int id) {
        try {
            Message m = new Message(41);
            m.writer().writeShort(id);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerAttackPlayer(int charId) {
        try {
            Message m = new Message(61);
            m.writer().writeInt(charId);
            this.connection.sendMessage(m);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void sendPlayerAttackMob(short mobId) {
        try {
            Message m = new Message(60);
            m.writer().writeByte(mobId);
            this.connection.sendMessage(m);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void requestJoinParty(String name) {
        try {
            Message m = new Message(23);
            m.writer().writeUTF(name);
            this.connection.sendMessage(m);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void acceptPleaseParty(String name) {
        try {
            Message m = new Message(24);
            m.writer().writeUTF(name);
            this.connection.sendMessage(m);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void createParty() {
        Message m = null;
        try {
            m = Service.messageSubCommand((byte)-88);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void inviteIntoParty(String name) {
        Message m = null;
        try {
            m = new Message(79);
            m.writer().writeUTF(name);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    public void leaveParty() {
        Message m = null;
        try {
            m = new Message(83);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void acceptRequestInviteIntoParty(int charId) {
        Message m = null;
        try {
            m = new Message(80);
            m.writer().writeInt(charId);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancelRequestInviteIntoParty(int charId) {
        Message m = null;
        try {
            m = new Message(81);
            m.writer().writeInt(charId);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void kickOutParty(int index) {
        Message m = null;
        try {
            m = Service.messageSubCommand((byte)-86);
            m.writer().writeByte(index);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            m.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void activeAccProtect(int pass) {
        Message message = null;
        try {
            message = this.messageNotMap((byte)-105);
            message.writer().writeInt(pass);
            this.connection.sendMessage(message);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            message.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void openLockAccProtect(int pass) {
        Message message = null;
        try {
            message = this.messageNotMap((byte)-103);
            message.writer().writeInt(pass);
            this.connection.sendMessage(message);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            message.cleanup();
        }
    }

    public void requestIcon(int id) {
        try {
            Message msg = this.messageNotMap((byte)-115);
            msg.writer().writeInt(id);
            this.connection.sendMessage(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openUIMenu(int npcID) {
        try {
            Message msg = new Message(40);
            msg.writer().writeShort(npcID);
            this.connection.sendMessage(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestFriends() {
        try {
            Message m = Service.messageSubCommand((byte)-85);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFriend(String name) {
        try {
            Message m = Service.messageSubCommand((byte)-83);
            m.writer().writeUTF(name);
            this.connection.sendMessage(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

