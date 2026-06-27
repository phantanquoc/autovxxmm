/*
 * Decompiled with CFR 0.152.
 */
package service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import utils.FileUtils;
import utils.Res;

public class SettingService {
    private static final String PATH = "records/setting.txt";
    private static final SettingService instance = new SettingService();
    private int loginLimit = 8;
    private String protectionCode;
    private boolean autoRegisterProtection;
    private boolean autoOpenProtection;
    private boolean protectBot = true;
    private int timeCreateOrder = 120;
    private int timeBlockSpam = 15;
    private int blockSpamAfter = 10;
    private boolean enableMessageOrder;
    private String messageOrder = "";
    private boolean enableMessageBet;
    private String messageBet = "";
    private boolean enableMessageLose;
    private String messageLose = "";
    private boolean enableMessageWin;
    private String messageWin = "";
    private boolean enableMessageReward;
    private String messageReward = "";
    private boolean enableMessageOrderError;
    private String messageOrderError = "";
    private boolean saveReturnPoint;
    private int mapSaveReturnPoint = 1;
    private boolean enableKeepBotOnline;
    private int minuteKeepBotOnline = 10;

    private SettingService() {
        this.load();
    }

    public void load() {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.read(PATH));
             DataInputStream dis = new DataInputStream(bais);){
            this.loginLimit = dis.readInt();
            this.protectionCode = dis.readUTF();
            this.autoRegisterProtection = dis.readBoolean();
            this.autoOpenProtection = dis.readBoolean();
            this.protectBot = dis.readBoolean();
            this.timeCreateOrder = dis.readInt();
            this.timeBlockSpam = dis.readInt();
            this.blockSpamAfter = dis.readInt();
            this.enableMessageOrder = dis.readBoolean();
            this.messageOrder = dis.readUTF();
            this.enableMessageBet = dis.readBoolean();
            this.messageBet = dis.readUTF();
            this.enableMessageLose = dis.readBoolean();
            this.messageLose = dis.readUTF();
            this.enableMessageWin = dis.readBoolean();
            this.messageWin = dis.readUTF();
            this.enableMessageReward = dis.readBoolean();
            this.messageReward = dis.readUTF();
            this.saveReturnPoint = dis.readBoolean();
            this.mapSaveReturnPoint = dis.readInt();
            this.enableMessageOrderError = dis.readBoolean();
            this.messageOrderError = dis.readUTF();
            this.enableKeepBotOnline = dis.readBoolean();
            this.minuteKeepBotOnline = dis.readInt();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void save() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos);){
            dos.writeInt(this.loginLimit);
            dos.writeUTF(this.protectionCode);
            dos.writeBoolean(this.autoRegisterProtection);
            dos.writeBoolean(this.autoOpenProtection);
            dos.writeBoolean(this.protectBot);
            dos.writeInt(this.timeCreateOrder);
            dos.writeInt(this.timeBlockSpam);
            dos.writeInt(this.blockSpamAfter);
            dos.writeBoolean(this.enableMessageOrder);
            dos.writeUTF(this.messageOrder);
            dos.writeBoolean(this.enableMessageBet);
            dos.writeUTF(this.messageBet);
            dos.writeBoolean(this.enableMessageLose);
            dos.writeUTF(this.messageLose);
            dos.writeBoolean(this.enableMessageWin);
            dos.writeUTF(this.messageWin);
            dos.writeBoolean(this.enableMessageReward);
            dos.writeUTF(this.messageReward);
            dos.writeBoolean(this.saveReturnPoint);
            dos.writeInt(this.mapSaveReturnPoint);
            dos.writeBoolean(this.enableMessageOrderError);
            dos.writeUTF(this.messageOrderError);
            dos.writeBoolean(this.enableKeepBotOnline);
            dos.writeInt(this.minuteKeepBotOnline);
            FileUtils.save(PATH, baos.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isValidProtectionCode() {
        return SettingService.isValidProtectionCode(this.protectionCode);
    }

    public static boolean isValidProtectionCode(String protectionCode) {
        return protectionCode != null && protectionCode.length() == 6 && Res.isNumber(protectionCode);
    }

    public long getTimeKeepBotOnline() {
        return (long)this.minuteKeepBotOnline * 60L * 1000L;
    }

    public int getLoginLimit() {
        return this.loginLimit;
    }

    public String getProtectionCode() {
        return this.protectionCode;
    }

    public boolean isAutoRegisterProtection() {
        return this.autoRegisterProtection;
    }

    public boolean isAutoOpenProtection() {
        return this.autoOpenProtection;
    }

    public boolean isProtectBot() {
        return this.protectBot;
    }

    public int getTimeCreateOrder() {
        return this.timeCreateOrder;
    }

    public int getTimeBlockSpam() {
        return this.timeBlockSpam;
    }

    public int getBlockSpamAfter() {
        return this.blockSpamAfter;
    }

    public boolean isEnableMessageOrder() {
        return this.enableMessageOrder;
    }

    public String getMessageOrder() {
        return this.messageOrder;
    }

    public boolean isEnableMessageBet() {
        return this.enableMessageBet;
    }

    public String getMessageBet() {
        return this.messageBet;
    }

    public boolean isEnableMessageLose() {
        return this.enableMessageLose;
    }

    public String getMessageLose() {
        return this.messageLose;
    }

    public boolean isEnableMessageWin() {
        return this.enableMessageWin;
    }

    public String getMessageWin() {
        return this.messageWin;
    }

    public boolean isEnableMessageReward() {
        return this.enableMessageReward;
    }

    public String getMessageReward() {
        return this.messageReward;
    }

    public boolean isEnableMessageOrderError() {
        return this.enableMessageOrderError;
    }

    public String getMessageOrderError() {
        return this.messageOrderError;
    }

    public boolean isSaveReturnPoint() {
        return this.saveReturnPoint;
    }

    public int getMapSaveReturnPoint() {
        return this.mapSaveReturnPoint;
    }

    public boolean isEnableKeepBotOnline() {
        return this.enableKeepBotOnline;
    }

    public int getMinuteKeepBotOnline() {
        return this.minuteKeepBotOnline;
    }

    public void setLoginLimit(int loginLimit) {
        this.loginLimit = loginLimit;
    }

    public void setProtectionCode(String protectionCode) {
        this.protectionCode = protectionCode;
    }

    public void setAutoRegisterProtection(boolean autoRegisterProtection) {
        this.autoRegisterProtection = autoRegisterProtection;
    }

    public void setAutoOpenProtection(boolean autoOpenProtection) {
        this.autoOpenProtection = autoOpenProtection;
    }

    public void setProtectBot(boolean protectBot) {
        this.protectBot = protectBot;
    }

    public void setTimeCreateOrder(int timeCreateOrder) {
        this.timeCreateOrder = timeCreateOrder;
    }

    public void setTimeBlockSpam(int timeBlockSpam) {
        this.timeBlockSpam = timeBlockSpam;
    }

    public void setBlockSpamAfter(int blockSpamAfter) {
        this.blockSpamAfter = blockSpamAfter;
    }

    public void setEnableMessageOrder(boolean enableMessageOrder) {
        this.enableMessageOrder = enableMessageOrder;
    }

    public void setMessageOrder(String messageOrder) {
        this.messageOrder = messageOrder;
    }

    public void setEnableMessageBet(boolean enableMessageBet) {
        this.enableMessageBet = enableMessageBet;
    }

    public void setMessageBet(String messageBet) {
        this.messageBet = messageBet;
    }

    public void setEnableMessageLose(boolean enableMessageLose) {
        this.enableMessageLose = enableMessageLose;
    }

    public void setMessageLose(String messageLose) {
        this.messageLose = messageLose;
    }

    public void setEnableMessageWin(boolean enableMessageWin) {
        this.enableMessageWin = enableMessageWin;
    }

    public void setMessageWin(String messageWin) {
        this.messageWin = messageWin;
    }

    public void setEnableMessageReward(boolean enableMessageReward) {
        this.enableMessageReward = enableMessageReward;
    }

    public void setMessageReward(String messageReward) {
        this.messageReward = messageReward;
    }

    public void setEnableMessageOrderError(boolean enableMessageOrderError) {
        this.enableMessageOrderError = enableMessageOrderError;
    }

    public void setMessageOrderError(String messageOrderError) {
        this.messageOrderError = messageOrderError;
    }

    public void setSaveReturnPoint(boolean saveReturnPoint) {
        this.saveReturnPoint = saveReturnPoint;
    }

    public void setMapSaveReturnPoint(int mapSaveReturnPoint) {
        this.mapSaveReturnPoint = mapSaveReturnPoint;
    }

    public void setEnableKeepBotOnline(boolean enableKeepBotOnline) {
        this.enableKeepBotOnline = enableKeepBotOnline;
    }

    public void setMinuteKeepBotOnline(int minuteKeepBotOnline) {
        this.minuteKeepBotOnline = minuteKeepBotOnline;
    }

    public static SettingService getInstance() {
        return instance;
    }
}

