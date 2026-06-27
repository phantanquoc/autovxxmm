/*
 * Decompiled with CFR 0.152.
 */
package network;

import core.cache.DataStream;
import core.cache.Skills;
import core.model.Char;
import core.model.Item;
import core.model.ItemMap;
import core.model.Mob;
import core.model.Npc;
import core.model.Party;
import core.model.Skill;
import core.model.Waypoint;
import core.template.ItemTemplate;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import lib.mVector;
import network.Connection;
import network.Message;
import service.BotService;
import utils.Res;

public class Controller {
    public Connection connection;
    private static int vsData;
    private static int vsMap;
    private static int vsSkill;
    private static int vsItem;

    public Controller(Connection connection) {
        this.connection = connection;
    }

    public void onMessage(Message msg) {
        try {
            Char chr = null;
            block22 : switch (msg.command) {
                case -28: {
                    this.messageNotMap(msg);
                    break;
                }
                case -30: {
                    this.messageSubCommand(msg);
                    break;
                }
                case 122: {
                    if (msg.reader().readByte() != 0) break;
                    this.addMob(msg);
                    break;
                }
                case -16: {
                    this.connection.bot.removeAllVector(false);
                    if (this.connection.bot.getParty().size() > 1) break;
                    this.connection.bot.getParty().removeAllElements();
                    break;
                }
                case -18: {
                    this.connection.bot.removeAllVector(false);
                    this.connection.bot.setLoading(true);
                    this.connection.bot.getTileMap().getvGo().removeAllElements();
                    System.gc();
                    this.connection.bot.getTileMap().setMapId((short)msg.reader().readUnsignedByte());
                    this.connection.bot.getTileMap().setTileID(msg.reader().readByte());
                    this.connection.bot.getTileMap().setBgID(msg.reader().readByte());
                    this.connection.bot.getTileMap().setTypeMap(msg.reader().readByte());
                    this.connection.bot.getTileMap().setMapName(msg.reader().readUTF());
                    this.connection.bot.getTileMap().setZoneId(msg.reader().readByte());
                    try {
                        this.connection.bot.getTileMap().loadMapFromResource();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    this.loadInfoMap(msg);
                    break;
                }
                case 3: {
                    chr = new Char();
                    chr.setCharId(msg.reader().readInt());
                    chr = this.readCharInfo(chr, msg);
                    if (chr == null) break;
                    if (this.connection.bot.getRole() == 0 && this.connection.getGameScreen().bot.isMapValid()) {
                        this.connection.bot.getLocalLog().saveCharJoinMap(chr);
                    }
                    this.connection.bot.getCharsInMap().addElement(chr);
                    break;
                }
                case 1: {
                    int charId = msg.reader().readInt();
                    chr = this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    short _x = msg.reader().readShort();
                    short _y = msg.reader().readShort();
                    chr.setPosX(_x);
                    chr.setCxFirst(_x);
                    chr.setPosY(_y);
                    chr.setCyFirst(_y);
                    break;
                }
                case 2: {
                    int charId = msg.reader().readInt();
                    this.connection.bot.getScreen().removeCharInMap(charId);
                    this.connection.bot.getScreen().partyClear(charId);
                    break;
                }
                case -56: {
                    int charId = msg.reader().readInt();
                    chr = this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    break;
                }
                case -55: {
                    int charId = msg.reader().readInt();
                    chr = this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    break;
                }
                case 4: 
                case 60: 
                case 61: {
                    break;
                }
                case -8: {
                    this.connection.bot.getMyChar().setKins(this.connection.bot.getMyChar().getKins() + msg.reader().readInt());
                    break;
                }
                case 95: {
                    this.connection.bot.getMyChar().setCoin(this.connection.bot.getMyChar().getCoin() + msg.reader().readInt());
                    break;
                }
                case -7: {
                    int num = msg.reader().readInt();
                    this.connection.bot.getMyChar().setCoin(this.connection.bot.getMyChar().getCoin() + num);
                    this.connection.bot.getMyChar().setKins(this.connection.bot.getMyChar().getKins() - num);
                    break;
                }
                case 5: {
                    long num66 = msg.reader().readLong();
                    this.connection.bot.getMyChar().setExpDown(0L);
                    this.connection.bot.getMyChar().setExp(this.connection.bot.getMyChar().getExp() + num66);
                    int clevel = this.connection.bot.getMyChar().getLevel();
                    this.connection.bot.getMyChar().setLevel_Exp();
                    break;
                }
                case 62: {
                    int charId = msg.reader().readInt();
                    chr = charId == this.connection.bot.getMyChar().getCharId() ? this.connection.bot.getMyChar() : this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    msg.reader().readInt();
                    try {
                        chr.setMp(msg.reader().readInt());
                        msg.reader().readInt();
                    }
                    catch (Exception exception) {}
                    break;
                }
                case -4: {
                    Mob mob = null;
                    try {
                        int id = msg.reader().readUnsignedByte();
                        mob = this.connection.getGameScreen().getMob(id);
                    }
                    catch (Exception id) {
                        // empty catch block
                    }
                    if (mob == null || mob.status == 0) break;
                    mob.hp = 0;
                    mob.status = 1;
                    try {
                        int num = msg.reader().readInt();
                        if (num < 0) {
                            num = Res.abs(num) + Short.MAX_VALUE;
                        }
                        ItemMap itemMap = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
                        this.connection.bot.getItemsMap().addElement(itemMap);
                    }
                    catch (Exception itemMap) {}
                    break;
                }
                case -5: {
                    try {
                        int id = msg.reader().readUnsignedByte();
                        Mob mob = this.connection.getGameScreen().getMob(id);
                        mob.sys = msg.reader().readByte();
                        mob.levelBoss = msg.reader().readByte();
                        mob.status = (byte)5;
                        mob.maxHp = mob.hp = msg.reader().readInt();
                    }
                    catch (Exception id) {}
                    break;
                }
                case -1: {
                    Mob mob = null;
                    try {
                        int iD = msg.reader().readUnsignedByte();
                        mob = this.connection.getGameScreen().getMob(iD);
                    }
                    catch (Exception iD) {
                        // empty catch block
                    }
                    if (mob == null) break;
                    mob.hp = msg.reader().readInt();
                    int num60 = msg.reader().readInt();
                    if (num60 < 0) {
                        num60 = Res.abs(num60) + Short.MAX_VALUE;
                    }
                    boolean flag = msg.reader().readBoolean();
                    try {
                        if (msg.reader().available() > 0) {
                            mob.levelBoss = msg.reader().readByte();
                        }
                        mob.maxHp = msg.reader().readInt();
                    }
                    catch (Exception exception) {}
                    break;
                }
                case 51: {
                    break;
                }
                case 78: {
                    Mob mob = null;
                    try {
                        int id = msg.reader().readUnsignedByte();
                        mob = this.connection.getGameScreen().getMob(id);
                    }
                    catch (Exception id) {
                        // empty catch block
                    }
                    if (mob == null || mob.status == 0) break;
                    mob.status = 0;
                    ItemMap itemMap = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
                    this.connection.bot.getItemsMap().addElement(itemMap);
                    break;
                }
                case -3: {
                    break;
                }
                case -2: {
                    Mob mob = null;
                    try {
                        int iD = msg.reader().readUnsignedByte();
                        mob = this.connection.getGameScreen().getMob(iD);
                    }
                    catch (Exception iD) {
                        // empty catch block
                    }
                    int charId2 = msg.reader().readInt();
                    int num14 = msg.reader().readInt();
                    if (mob == null) break;
                    Char char4 = this.connection.getGameScreen().findCharInMap(charId2);
                    if (char4 == null) {
                        return;
                    }
                    int dame = char4.getHp() - num14;
                    char4.setHp(num14);
                    try {
                        char4.setMp(msg.reader().readInt());
                    }
                    catch (Exception exception) {}
                    break;
                }
                case 85: {
                    Mob mob = this.connection.getGameScreen().getMob(msg.reader().readUnsignedByte());
                    if (mob == null) break;
                    mob.isDisable = msg.reader().readBoolean();
                    break;
                }
                case 86: {
                    Mob mob = this.connection.getGameScreen().getMob(msg.reader().readUnsignedByte());
                    if (mob == null) break;
                    mob.isDontMove = msg.reader().readBoolean();
                    break;
                }
                case 89: {
                    Mob mob = this.connection.getGameScreen().getMob(msg.reader().readUnsignedByte());
                    if (mob == null) break;
                    mob.isFire = msg.reader().readBoolean();
                    break;
                }
                case 90: {
                    Mob mob = this.connection.getGameScreen().getMob(msg.reader().readUnsignedByte());
                    if (mob == null) break;
                    mob.isIce = msg.reader().readBoolean();
                    break;
                }
                case 91: {
                    Mob mob = this.connection.getGameScreen().getMob(msg.reader().readUnsignedByte());
                    if (mob == null) break;
                    mob.isWind = msg.reader().readBoolean();
                    break;
                }
                case 88: {
                    int charId = msg.reader().readInt();
                    chr = charId == this.connection.bot.getMyChar().getCharId() ? this.connection.bot.getMyChar() : this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    chr.setHp(chr.getMaxHP());
                    chr.setMp(chr.getMaxMP());
                    short _x = msg.reader().readShort();
                    short _y = msg.reader().readShort();
                    chr.setPosX(_x);
                    chr.setCxFirst(_x);
                    chr.setPosY(_y);
                    chr.setCyFirst(_y);
                    break;
                }
                case 52: {
                    this.connection.bot.getMyChar().setPosX(msg.reader().readShort());
                    this.connection.bot.getMyChar().setPosY(msg.reader().readShort());
                    break;
                }
                case 8: {
                    byte num8 = msg.reader().readByte();
                    this.connection.bot.getMyChar().getArrItemBag()[num8] = new Item();
                    this.connection.bot.getMyChar().getArrItemBag()[num8].typeUI = 3;
                    this.connection.bot.getMyChar().getArrItemBag()[num8].indexUI = num8;
                    this.connection.bot.getMyChar().getArrItemBag()[num8].template = Item.get(msg.reader().readShort());
                    this.connection.bot.getMyChar().getArrItemBag()[num8].isLock = msg.reader().readBoolean();
                    if (this.connection.bot.getMyChar().getArrItemBag()[num8].isTypeBody() || this.connection.bot.getMyChar().getArrItemBag()[num8].isTypeNgocKham()) {
                        this.connection.bot.getMyChar().getArrItemBag()[num8].upgrade = msg.reader().readByte();
                    }
                    this.connection.bot.getMyChar().getArrItemBag()[num8].isExpires = msg.reader().readBoolean();
                    try {
                        this.connection.bot.getMyChar().getArrItemBag()[num8].quantity = msg.reader().readUnsignedShort();
                    }
                    catch (Exception e) {
                        this.connection.bot.getMyChar().getArrItemBag()[num8].quantity = 1;
                    }
                    break;
                }
                case 10: {
                    byte num8 = msg.reader().readByte();
                    this.connection.bot.getMyChar().getArrItemBag()[num8] = null;
                    break;
                }
                case 18: {
                    byte num8 = msg.reader().readByte();
                    short num4 = 1;
                    try {
                        num4 = msg.reader().readShort();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (this.connection.bot.getMyChar().getArrItemBag()[num8].quantity > num4) {
                        this.connection.bot.getMyChar().getArrItemBag()[num8].quantity -= num4;
                        break;
                    }
                    this.connection.bot.getMyChar().getArrItemBag()[num8] = null;
                    break;
                }
                case 9: {
                    Item item4 = this.connection.bot.getMyChar().getArrItemBag()[msg.reader().readUnsignedByte()];
                    short num4 = 0;
                    try {
                        num4 = msg.reader().readShort();
                    }
                    catch (Exception e) {
                        num4 = 1;
                    }
                    item4.quantity += num4;
                    break;
                }
                case 15: {
                    break;
                }
                case 16: {
                    try {
                        int num = msg.reader().readUnsignedByte();
                        int num2 = msg.reader().readUnsignedByte();
                        Item item = this.connection.bot.getMyChar().getArrItemBox()[num];
                        if (item != null) {
                            this.connection.bot.getMyChar().getArrItemBox()[num] = null;
                            if (this.connection.bot.getMyChar().getArrItemBag()[num2] == null) {
                                item.indexUI = num2;
                                item.typeUI = 3;
                                this.connection.bot.getMyChar().getArrItemBag()[num2] = item;
                                break;
                            }
                            this.connection.bot.getMyChar().getArrItemBag()[num2].quantity += item.quantity;
                        }
                    }
                    catch (Exception num2) {}
                    break;
                }
                case 17: {
                    try {
                        int num = msg.reader().readUnsignedByte();
                        int num2 = msg.reader().readUnsignedByte();
                        Item item = this.connection.bot.getMyChar().getArrItemBag()[num];
                        if (item != null) {
                            this.connection.bot.getMyChar().getArrItemBag()[num] = null;
                            if (this.connection.bot.getMyChar().getArrItemBox()[num2] == null) {
                                item.indexUI = num2;
                                item.typeUI = 4;
                                this.connection.bot.getMyChar().getArrItemBox()[num2] = item;
                                break;
                            }
                            this.connection.bot.getMyChar().getArrItemBox()[num2].quantity += item.quantity;
                        }
                    }
                    catch (Exception num2) {}
                    break;
                }
                case 112: {
                    Item item3 = this.connection.bot.getMyChar().getArrItemBag()[msg.reader().readByte()];
                    item3.upgrade = msg.reader().readByte();
                    item3.expires = 0L;
                    break;
                }
                case 11: {
                    byte num8 = msg.reader().readByte();
                    Item item = this.connection.bot.getMyChar().getArrItemBag()[num8];
                    if (item.isTypeBody()) {
                        item.isLock = true;
                        item.typeUI = 5;
                        Item item2 = this.connection.bot.getMyChar().getArrItemBody()[item.template.type];
                        this.connection.bot.getMyChar().getArrItemBag()[num8] = null;
                        if (item2 != null) {
                            item2.typeUI = 3;
                            this.connection.bot.getMyChar().getArrItemBody()[item.template.type] = null;
                            item2.indexUI = num8;
                            this.connection.bot.getMyChar().getArrItemBag()[num8] = item2;
                        }
                        item.indexUI = item.template.type;
                        this.connection.bot.getMyChar().getArrItemBody()[item.indexUI] = item;
                    }
                    this.connection.bot.getMyChar().readParam(msg, "Cmd.ITEM_USE");
                    this.connection.bot.getMyChar().setEff5BuffHp(msg.reader().readShort());
                    this.connection.bot.getMyChar().setEff5BuffMp(msg.reader().readShort());
                    break;
                }
                case 25: {
                    int n = msg.reader().readByte();
                    for (int i = 0; i < n; ++i) {
                        int id = msg.reader().readInt();
                        short newX = msg.reader().readShort();
                        short newY = msg.reader().readShort();
                        int newHP = msg.reader().readInt();
                        chr = this.connection.getGameScreen().findCharInMap(id);
                        if (chr == null) continue;
                        chr.setPosX(newX);
                        chr.setCxFirst(newX);
                        chr.setPosY(newY);
                        chr.setCyFirst(newY);
                        chr.setHp(newHP);
                    }
                    break;
                }
                case 68: {
                    Char char4 = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (char4 == null) break;
                    char4.killCharId = this.connection.bot.getMyChar().getCharId();
                    break;
                }
                case 70: {
                    Char char4 = this.connection.bot.getMyChar();
                    try {
                        char4 = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    }
                    catch (Exception id) {
                        // empty catch block
                    }
                    char4.killCharId = -9999;
                    break;
                }
                case 27: {
                    break;
                }
                case 64: {
                    int num9 = msg.reader().readInt();
                    Char c = num9 != this.connection.bot.getMyChar().getCharId() ? this.connection.getGameScreen().findCharInMap(num9) : this.connection.bot.getMyChar();
                    short num83 = msg.reader().readShort();
                    short num84 = msg.reader().readShort();
                    try {
                        num9 = msg.reader().readInt();
                        Char char2 = num9 != this.connection.bot.getMyChar().getCharId() ? this.connection.getGameScreen().findCharInMap(num9) : this.connection.bot.getMyChar();
                        char2.setPosX(num83);
                        char2.setPosY(num84);
                    }
                    catch (Exception char2) {}
                    break;
                }
                case 38: {
                    short npcId = msg.reader().readShort();
                    String npcSay = msg.reader().readUTF();
                    if (!this.connection.bot.getWaitAction().isWaitSaveMapReturnTown || npcId != 5 || !npcSay.equals("T\u1ed1t l\u1eafm, ng\u01b0\u01a1i \u0111\u00e3 ch\u1ecdn n\u01a1i n\u00e0y l\u00e0m n\u01a1i tr\u1edf v\u1ec1 khi b\u1ecb tr\u1ecdng th\u01b0\u01a1ng")) break;
                    this.connection.getGameScreen().orderScreen().setMapSaveReturnPoint(this.connection.bot.getTileMap().getMapId());
                    this.connection.bot.getWaitAction().notifySaveMapReturnTown();
                    break;
                }
                case 92: {
                    break;
                }
                case 34: {
                    break;
                }
                case 47: {
                    break;
                }
                case 48: {
                    break;
                }
                case 49: {
                    break;
                }
                case 50: {
                    break;
                }
                case 93: {
                    break;
                }
                case 42: {
                    this.requestItemInfo(msg);
                    break;
                }
                case 94: {
                    break;
                }
                case 36: {
                    break;
                }
                case -15: {
                    short num18 = msg.reader().readShort();
                    for (int num53 = 0; num53 < this.connection.bot.getItemsMap().size(); ++num53) {
                        ItemMap itemMap5 = (ItemMap)this.connection.bot.getItemsMap().elementAt(num53);
                        if (itemMap5 == null || itemMap5.itemMapID != num18) continue;
                        this.connection.bot.getItemsMap().removeElementAt(num53);
                        break block22;
                    }
                    break;
                }
                case -14: {
                    short num18 = msg.reader().readShort();
                    for (int n = 0; n < this.connection.bot.getItemsMap().size(); n = (int)((byte)(n + 1))) {
                        ItemMap itemMap3 = (ItemMap)this.connection.bot.getItemsMap().elementAt(n);
                        if (itemMap3.itemMapID != num18 || itemMap3.template.type != 19) continue;
                        int num = msg.reader().readUnsignedShort();
                        this.connection.bot.getMyChar().setKins(this.connection.bot.getMyChar().getKins() + num);
                    }
                    break;
                }
                case -13: {
                    break;
                }
                case 6: {
                    ItemMap itemMap = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
                    this.connection.bot.getItemsMap().addElement(itemMap);
                    break;
                }
                case 7: {
                    this.connection.bot.getMyChar().getArrItemBag()[msg.reader().readByte()].quantity = msg.reader().readShort();
                    break;
                }
                case 75: {
                    break;
                }
                case -6: {
                    Char char4 = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (char4 == null) {
                        return;
                    }
                    this.connection.bot.getItemsMap().addElement(new ItemMap(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort()));
                    break;
                }
                case 13: {
                    this.connection.bot.getMyChar().setCoin(msg.reader().readInt());
                    this.connection.bot.getMyChar().setKins(msg.reader().readInt());
                    this.connection.bot.getMyChar().setGold(msg.reader().readInt());
                    break;
                }
                case 14: {
                    Item item = this.connection.bot.getMyChar().getArrItemBag()[msg.reader().readByte()];
                    this.connection.bot.getMyChar().setKins(msg.reader().readInt());
                    short num4 = 0;
                    try {
                        num4 = msg.reader().readShort();
                    }
                    catch (Exception e) {
                        num4 = 1;
                    }
                    item.quantity -= num4;
                    if (item.quantity > 0) break;
                    this.connection.bot.getMyChar().getArrItemBag()[item.indexUI] = null;
                    break;
                }
                case 71: {
                    this.connection.bot.getMyChar().setExpDown(this.connection.bot.getMyChar().getExpDown() - msg.reader().readLong());
                    break;
                }
                case 116: {
                    int charId4 = msg.reader().readInt();
                    Char char4 = this.connection.getGameScreen().findCharInMap(charId4);
                    if (char4 == null) break;
                    this.readCharInfo(char4, msg);
                    break;
                }
                case -22: {
                    if (this.connection.bot.getMessageStream() == null) break;
                    this.connection.bot.getMessageStream().receivePrivateChat(msg.reader().readUTF(), msg.reader().readUTF());
                    break;
                }
                case -26: {
                    this.connection.bot.getScreen().handleServerDialogMessage(msg.reader().readUTF());
                    this.connection.bot.getWaitAction().notifyWaitMap();
                    break;
                }
                case -25: {
                    this.connection.bot.getScreen().handleServerAlertMessage(msg.reader().readUTF());
                    break;
                }
                case -24: {
                    this.connection.getGameScreen().handleServerMessage(msg.reader().readUTF());
                    break;
                }
                case 53: {
                    String title = msg.reader().readUTF();
                    if (!title.equals("typemoi")) break;
                    if (this.connection.bot.getTrade().isShow()) {
                        this.connection.getService().cancelTrade(false);
                    }
                    String title_1 = msg.reader().readUTF();
                    short time = msg.reader().readShort();
                    String totalMoney = msg.reader().readUTF();
                    short percentWin1 = msg.reader().readShort();
                    String percentWin2 = msg.reader().readUTF();
                    short numPlayer = msg.reader().readShort();
                    String winnerInfo = msg.reader().readUTF();
                    byte typeLucky = msg.reader().readByte();
                    String myMoney = msg.reader().readUTF();
                    this.connection.bot.getLuckyDraw().show(title_1, time, totalMoney, percentWin1, percentWin2, numPlayer, winnerInfo, myMoney, typeLucky);
                    break;
                }
                case 23: {
                    String _name = msg.reader().readUTF();
                    this.connection.bot.getScreen().handleRequestJoinParty(_name);
                    break;
                }
                case 79: {
                    int pId = msg.reader().readInt();
                    String pName = msg.reader().readUTF();
                    this.connection.bot.getScreen().handleRequestInviteIntoParty(pName, pId);
                    break;
                }
                case 82: {
                    this.connection.bot.getParty().removeAllElements();
                    boolean isLock = msg.reader().readBoolean();
                    try {
                        for (int i = 0; i < 6; ++i) {
                            this.connection.bot.getParty().addElement(new Party(this.connection.bot, msg.reader().readInt(), msg.reader().readByte(), msg.reader().readUTF(), isLock));
                        }
                        break;
                    }
                    catch (Exception i) {
                        break;
                    }
                }
                case 83: {
                    this.connection.bot.getParty().removeAllElements();
                    break;
                }
                case 37: {
                    this.connection.bot.getTrade().start(msg.reader().readUTF());
                    break;
                }
                case 43: {
                    int charId = msg.reader().readInt();
                    chr = this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    this.connection.bot.getTrade().handleInvite(chr);
                    break;
                }
                case 45: {
                    this.connection.bot.getTrade().typeTradeOrder = 1;
                    this.connection.bot.getTrade().coinTradeOrder = msg.reader().readInt();
                    this.connection.bot.getTrade().arrItemTradeOrder = new Item[12];
                    int size = msg.reader().readByte();
                    for (int i = 0; i < size; ++i) {
                        this.connection.bot.getTrade().arrItemTradeOrder[i] = new Item();
                        this.connection.bot.getTrade().arrItemTradeOrder[i].indexUI = i;
                        this.connection.bot.getTrade().arrItemTradeOrder[i].template = Item.get(msg.reader().readShort());
                        this.connection.bot.getTrade().arrItemTradeOrder[i].isLock = false;
                        if (this.connection.bot.getTrade().arrItemTradeOrder[i].isTypeBody()) {
                            this.connection.bot.getTrade().arrItemTradeOrder[i].upgrade = msg.reader().readByte();
                        }
                        this.connection.bot.getTrade().arrItemTradeOrder[i].isExpires = msg.reader().readBoolean();
                        this.connection.bot.getTrade().arrItemTradeOrder[i].quantity = msg.reader().readShort();
                        this.connection.bot.getTrade().haveItemTradeOrder = true;
                    }
                    break;
                }
                case 46: {
                    this.connection.bot.getTrade().typeTradeOrder = 2;
                    break;
                }
                case 58: {
                    this.connection.bot.getMyChar().setCoin(msg.reader().readInt());
                    this.connection.bot.getTrade().setTradeSuccess(true);
                    this.connection.bot.getTrade().success();
                    this.connection.getService().cancelTrade(false);
                    this.connection.bot.getTrade().reset();
                    break;
                }
                case 57: {
                    this.connection.bot.getTrade().addSpam();
                    this.connection.bot.getTrade().reset();
                    break;
                }
                case 59: {
                    String name = msg.reader().readUTF();
                    break;
                }
                case 63: {
                    mVector vmenu = new mVector("vMenu");
                    try {
                        while (true) {
                            String menu = msg.reader().readUTF();
                            vmenu.addElement(menu);
                        }
                    }
                    catch (Exception e) {
                        for (int i = 0; i < vmenu.size(); ++i) {
                            String menu = (String)vmenu.elementAt(i);
                            if (!menu.equals("V\u00f2ng xoay th\u01b0\u1eddng") && !menu.equals("Lucky draw") && !menu.equals("Th\u00f4ng tin")) continue;
                            this.connection.getService().menu(0, i, 0);
                            break block22;
                        }
                        break;
                    }
                }
                case -11: {
                    this.connection.bot.getMyChar().setPk(msg.reader().readByte());
                    this.connection.bot.getMyChar().waitToDie(msg.reader().readShort(), msg.reader().readShort());
                    try {
                        this.connection.bot.getMyChar().setExp(msg.reader().readLong());
                        this.connection.bot.getMyChar().setLevel_Exp();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.connection.bot.getWaitAction().notifyDie();
                    break;
                }
                case 72: {
                    this.connection.bot.getMyChar().setPk(msg.reader().readByte());
                    this.connection.bot.getMyChar().waitToDie(msg.reader().readShort(), msg.reader().readShort());
                    this.connection.bot.getMyChar().setExp(Res.getMaxExp(this.connection.bot.getMyChar().getLevel() - 1));
                    this.connection.bot.getMyChar().setExpDown(msg.reader().readLong());
                    this.connection.bot.getMyChar().setLevel_Exp();
                    break;
                }
                case 0: {
                    chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) {
                        return;
                    }
                    chr.setPk(msg.reader().readByte());
                    chr.waitToDie(msg.reader().readShort(), msg.reader().readShort());
                    break;
                }
                case -10: {
                    if (this.connection.bot.getMyChar().getCdx() != 0 || this.connection.bot.getMyChar().getCdy() != 0) {
                        this.connection.bot.getMyChar().setPosX(this.connection.bot.getMyChar().getCdx());
                        this.connection.bot.getMyChar().setPosY(this.connection.bot.getMyChar().getCdy());
                        this.connection.bot.getMyChar().waitToDie(0, 0);
                    }
                    this.connection.bot.getMyChar().liveFromDead();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void messageNotMap(Message msg) {
        try {
            byte subcmd = msg.reader().readByte();
            switch (subcmd) {
                case -106: {
                    this.connection.bot.getMyChar().setTypeActive(msg.reader().readByte());
                    if (this.connection.bot.getMyChar().getTypeActive() != 2) break;
                    this.connection.debug("\u0110\u00c3 M\u1ede M\u00c3 B\u1ea2O V\u1ec6");
                    break;
                }
                case -117: {
                    this.connection.bot.getMyChar().setPk(msg.reader().readByte());
                    break;
                }
                case -126: {
                    byte size = msg.reader().readByte();
                    String[] name = new String[size];
                    for (byte i = 0; i < size; i = (byte)(i + 1)) {
                        msg.reader().readByte();
                        name[i] = msg.reader().readUTF();
                        msg.reader().readUTF();
                        msg.reader().readUnsignedByte();
                        msg.reader().readShort();
                        msg.reader().readShort();
                        msg.reader().readShort();
                        msg.reader().readShort();
                    }
                    String _name = name[0];
                    for (int i = 0; i < name.length; ++i) {
                        if (this.connection.bot.getCharName() == null || this.connection.bot.getCharName().isEmpty() || name[i] == null || !name[i].equals(this.connection.bot.getCharName())) continue;
                        _name = name[i];
                        break;
                    }
                    this.connection.bot.setPlayerName(_name);
                    this.connection.bot.getWaitAction().notifyConnection();
                    break;
                }
                case -123: {
                    byte vsData = msg.reader().readByte();
                    byte vsMap = msg.reader().readByte();
                    byte vsSkill = msg.reader().readByte();
                    byte vsItem = msg.reader().readByte();
                    if (!DataStream.isLoadData) {
                        this.connection.getService().updateData();
                    }
                    if (!DataStream.isLoadMap) {
                        this.connection.getService().updateMap();
                    }
                    if (!DataStream.isLoadSkill) {
                        this.connection.getService().updateSkill();
                    }
                    if (!DataStream.isLoadItem) {
                        this.connection.getService().updateItem();
                    }
                    this.connection.getService().clientOk();
                    break;
                }
                case -122: {
                    msg.reader().mark(100000);
                    msg.reader().reset();
                    byte[] data = new byte[msg.reader().available()];
                    msg.reader().readFully(data);
                    DataStream.loadData(data);
                    break;
                }
                case -121: {
                    msg.reader().mark(100000);
                    msg.reader().reset();
                    byte[] data = new byte[msg.reader().available()];
                    msg.reader().readFully(data);
                    DataStream.loadMap(data);
                    break;
                }
                case -120: {
                    msg.reader().mark(100000);
                    msg.reader().reset();
                    byte[] data = new byte[msg.reader().available()];
                    msg.reader().readFully(data);
                    DataStream.loadSkill(data);
                    break;
                }
                case -119: {
                    msg.reader().mark(100000);
                    msg.reader().reset();
                    byte[] data = new byte[msg.reader().available()];
                    msg.reader().readFully(data);
                    DataStream.loadItem(data);
                    break;
                }
                case -108: {
                    short mobTemplateId = msg.reader().readShort();
                    try {
                        byte typeMobFly;
                        Mob.arrMobTemplate[mobTemplateId].typeFly = typeMobFly = msg.reader().readByte();
                    }
                    catch (Exception typeMobFly) {
                        // empty catch block
                    }
                    byte sizeImg = msg.reader().readByte();
                    Mob.arrMobTemplate[mobTemplateId].imgs = new BufferedImage[sizeImg];
                    if (mobTemplateId != 219) break;
                    for (int i = 0; i < Mob.arrMobTemplate[mobTemplateId].imgs.length; ++i) {
                        byte[] bs = new byte[msg.reader().readInt()];
                        msg.reader().read(bs);
                        ByteArrayInputStream ins = new ByteArrayInputStream(bs);
                        Mob.arrMobTemplate[mobTemplateId].imgs[i] = ImageIO.read(ins);
                    }
                    this.connection.getGameScreen().parseGhost(Mob.arrMobTemplate[mobTemplateId].imgs[0]);
                    break;
                }
                case -109: {
                    this.connection.bot.getTileMap().setMaps(null);
                    this.connection.bot.getTileMap().setTypes(null);
                    this.connection.bot.getTileMap().setTmw(msg.reader().readByte());
                    this.connection.bot.getTileMap().setTmh(msg.reader().readByte());
                    this.connection.bot.getTileMap().setMaps(new char[this.connection.bot.getTileMap().getTmw() * this.connection.bot.getTileMap().getTmh()]);
                    for (int l = 0; l < this.connection.bot.getTileMap().getMaps().length; ++l) {
                        int num4 = msg.reader().readByte();
                        if (num4 < 0) {
                            num4 += 256;
                        }
                        this.connection.bot.getTileMap().getMaps()[l] = (char)num4;
                    }
                    this.connection.bot.getTileMap().setTypes(new int[this.connection.bot.getTileMap().getMaps().length]);
                    this.loadInfoMap(msg);
                    break;
                }
                case -115: {
                    int id = msg.reader().readInt();
                    if (id != 1) break;
                    this.connection.bot.getHeartBeat().alive();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void messageSubCommand(Message msg) {
        try {
            byte subcmd = msg.reader().readByte();
            block5 : switch (subcmd) {
                case -127: 
                case 115: {
                    short itemTemplateId;
                    int i;
                    this.connection.bot.getMyChar().setCharId(msg.reader().readInt());
                    this.connection.bot.getMyChar().setClanName(msg.reader().readUTF());
                    if (!this.connection.bot.getMyChar().getClanName().equals("")) {
                        this.connection.bot.getMyChar().setTypeClan(msg.reader().readByte());
                    }
                    this.connection.bot.getMyChar().setTaskId(msg.reader().readByte());
                    this.connection.bot.getMyChar().setGender(msg.reader().readByte());
                    this.connection.bot.getMyChar().setHead(msg.reader().readShort());
                    this.connection.bot.getMyChar().setSpeed(msg.reader().readByte());
                    String name = msg.reader().readUTF();
                    this.connection.bot.getMyChar().setName(name);
                    this.connection.bot.getMyChar().setNameInternal(name);
                    this.connection.bot.getMyChar().setPk(msg.reader().readByte());
                    this.connection.bot.getMyChar().setTypePk(msg.reader().readByte());
                    this.connection.bot.getMyChar().setMaxHP(msg.reader().readInt());
                    this.connection.bot.getMyChar().setHp(msg.reader().readInt());
                    this.connection.bot.getMyChar().setMaxMP(msg.reader().readInt());
                    this.connection.bot.getMyChar().setMp(msg.reader().readInt());
                    this.connection.bot.getMyChar().setExp(msg.reader().readLong());
                    this.connection.bot.getMyChar().setExpDown(msg.reader().readLong());
                    this.connection.bot.getMyChar().setLevel_Exp();
                    this.connection.bot.getMyChar().setEff5BuffHp(msg.reader().readShort());
                    this.connection.bot.getMyChar().setEff5BuffMp(msg.reader().readShort());
                    this.connection.bot.getMyChar().setNinjaClassId(msg.reader().readByte());
                    this.connection.bot.getMyChar().setPotentialPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().setPotential(new int[4]);
                    this.connection.bot.getMyChar().getPotential()[0] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[1] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[2] = msg.reader().readInt();
                    this.connection.bot.getMyChar().getPotential()[3] = msg.reader().readInt();
                    this.connection.bot.getMyChar().setSkillPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().vSkill.removeAllElements();
                    int size = msg.reader().readByte();
                    for (i = 0; i < size; i = (byte)(i + 1)) {
                        short skillId = msg.reader().readShort();
                        Skill skill = Skills.get(skillId);
                        this.connection.bot.getMyChar().vSkill.addElement(skill);
                        if (skill.template.type != 1) continue;
                        this.connection.getService().selectSkill(skillId);
                    }
                    this.connection.bot.getMyChar().setCoin(msg.reader().readInt());
                    this.connection.bot.getMyChar().setKins(msg.reader().readInt());
                    this.connection.bot.getMyChar().setGold(msg.reader().readInt());
                    this.connection.bot.getMyChar().setArrItemBag(new Item[msg.reader().readUnsignedByte()]);
                    try {
                        for (i = 0; i < this.connection.bot.getMyChar().getArrItemBag().length; ++i) {
                            itemTemplateId = msg.reader().readShort();
                            if (itemTemplateId == -1) continue;
                            this.connection.bot.getMyChar().getArrItemBag()[i] = new Item();
                            this.connection.bot.getMyChar().getArrItemBag()[i].indexUI = i;
                            this.connection.bot.getMyChar().getArrItemBag()[i].template = Item.get(itemTemplateId);
                            this.connection.bot.getMyChar().getArrItemBag()[i].isLock = msg.reader().readBoolean();
                            if (this.connection.bot.getMyChar().getArrItemBag()[i].isTypeBody() || this.connection.bot.getMyChar().getArrItemBag()[i].isTypeMounts() || this.connection.bot.getMyChar().getArrItemBag()[i].isTypeNgocKham()) {
                                this.connection.bot.getMyChar().getArrItemBag()[i].upgrade = msg.reader().readByte();
                            }
                            this.connection.bot.getMyChar().getArrItemBag()[i].isExpires = msg.reader().readBoolean();
                            this.connection.bot.getMyChar().getArrItemBag()[i].quantity = msg.reader().readUnsignedShort();
                        }
                    }
                    catch (Exception i2) {
                        // empty catch block
                    }
                    this.connection.bot.getMyChar().setArrItemBody(new Item[16]);
                    try {
                        for (int i3 = 0; i3 < this.connection.bot.getMyChar().getArrItemBody().length; ++i3) {
                            itemTemplateId = msg.reader().readShort();
                            if (itemTemplateId == -1) continue;
                            ItemTemplate template = Item.get(itemTemplateId);
                            byte indexUI = template.type;
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI] = new Item();
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI].indexUI = indexUI;
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI].template = template;
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI].isLock = true;
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI].upgrade = msg.reader().readByte();
                            this.connection.bot.getMyChar().getArrItemBody()[indexUI].sys = msg.reader().readByte();
                        }
                        break;
                    }
                    catch (Exception i3) {
                        break;
                    }
                }
                case -126: {
                    this.connection.bot.getMyChar().readParam(msg, "Cmd.ME_LOAD_SKILL");
                    this.connection.bot.getMyChar().getPotential()[0] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[1] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[2] = msg.reader().readInt();
                    this.connection.bot.getMyChar().getPotential()[3] = msg.reader().readInt();
                    this.connection.bot.getMyChar().setNinjaClassId(msg.reader().readByte());
                    this.connection.bot.getMyChar().setSkillPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().setPotentialPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().vSkill.removeAllElements();
                    break;
                }
                case -124: {
                    this.connection.bot.getMyChar().readParam(msg, "Cmd.ME_LOAD_LEVEL");
                    this.connection.bot.getMyChar().setExp(msg.reader().readLong());
                    this.connection.bot.getMyChar().setSkillPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().setPotentialPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().getPotential()[0] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[1] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[2] = msg.reader().readInt();
                    this.connection.bot.getMyChar().getPotential()[3] = msg.reader().readInt();
                    break;
                }
                case -123: {
                    this.connection.bot.getMyChar().setCoin(msg.reader().readInt());
                    this.connection.bot.getMyChar().setKins(msg.reader().readInt());
                    this.connection.bot.getMyChar().setGold(msg.reader().readInt());
                    this.connection.bot.getMyChar().setHp(msg.reader().readInt());
                    this.connection.bot.getMyChar().setMp(msg.reader().readInt());
                    byte isCaptCha = msg.reader().readByte();
                    if (isCaptCha == 1) {
                        this.connection.getGameScreen().isCaptcha = true;
                        break;
                    }
                    this.connection.getGameScreen().isCaptcha = false;
                    this.connection.bot.getWaitAction().notifyMa();
                    break;
                }
                case -122: {
                    this.connection.bot.getMyChar().setHp(msg.reader().readInt());
                    break;
                }
                case -121: {
                    this.connection.bot.getMyChar().setMp(msg.reader().readInt());
                    break;
                }
                case -125: {
                    this.connection.bot.getMyChar().readParam(msg, "Cmd.ME_LOAD_SKILL");
                    if (this.connection.bot.getMyChar().getStatusMe() != Char.A_DEAD && this.connection.bot.getMyChar().getStatusMe() != Char.A_DEADFLY) {
                        this.connection.bot.getMyChar().setHp(this.connection.bot.getMyChar().getMaxHP());
                        this.connection.bot.getMyChar().setMp(this.connection.bot.getMyChar().getMaxMP());
                    }
                    this.connection.bot.getMyChar().setSkillPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().vSkill.removeAllElements();
                    int size = msg.reader().readByte();
                    for (int i = 0; i < size; ++i) {
                        short skillId = msg.reader().readShort();
                        Skill skill = Skills.get(skillId);
                        this.connection.bot.getMyChar().vSkill.addElement(skill);
                        if (skill.template.type != 1) continue;
                        this.connection.getService().selectSkill(skillId);
                    }
                }
                case -109: {
                    this.connection.bot.getMyChar().readParam(msg, "Cmd.ME_LOAD_SKILL");
                    if (this.connection.bot.getMyChar().getStatusMe() != Char.A_DEAD && this.connection.bot.getMyChar().getStatusMe() != Char.A_DEADFLY) {
                        this.connection.bot.getMyChar().setHp(this.connection.bot.getMyChar().getMaxHP());
                        this.connection.bot.getMyChar().setMp(this.connection.bot.getMyChar().getMaxMP());
                    }
                    this.connection.bot.getMyChar().setPotentialPoint(msg.reader().readShort());
                    this.connection.bot.getMyChar().getPotential()[0] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[1] = msg.reader().readShort();
                    this.connection.bot.getMyChar().getPotential()[2] = msg.reader().readInt();
                    this.connection.bot.getMyChar().getPotential()[3] = msg.reader().readInt();
                    break;
                }
                case -120: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr = this.readCharInfo(chr, msg);
                    break;
                }
                case -119: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    break;
                }
                case -128: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    chr.setLevel(msg.reader().readUnsignedByte());
                    break;
                }
                case -117: 
                case -116: 
                case -113: 
                case -112: 
                case -64: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    chr.setEff5BuffHp(msg.reader().readShort());
                    chr.setEff5BuffMp(msg.reader().readShort());
                    try {
                        msg.reader().readShort();
                    }
                    catch (Exception i) {}
                    break;
                }
                case -111: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    break;
                }
                case -110: {
                    Char chr = this.connection.getGameScreen().findCharInMap(msg.reader().readInt());
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    short _x = msg.reader().readShort();
                    short _y = msg.reader().readShort();
                    chr.setPosX(_x);
                    chr.setPosY(_y);
                    chr.setCxFirst(_x);
                    chr.setCyFirst(_y);
                    chr.setStatusMe(Char.A_STAND);
                    break;
                }
                case -59: {
                    int charId = msg.reader().readInt();
                    Char chr = charId == this.connection.bot.getMyChar().getCharId() ? this.connection.bot.getMyChar() : this.connection.getGameScreen().findCharInMap(charId);
                    if (chr == null) break;
                    chr.setHp(msg.reader().readInt());
                    chr.setMaxHP(msg.reader().readInt());
                    break;
                }
                case -101: {
                    break;
                }
                case -100: {
                    break;
                }
                case -99: {
                    break;
                }
                case -92: {
                    Char chr;
                    int charId = msg.reader().readInt();
                    Char char_ = chr = charId != this.connection.bot.getMyChar().getCharId() ? this.connection.getGameScreen().findCharInMap(charId) : this.connection.bot.getMyChar();
                    if (chr == null) break;
                    chr.setTypePk(msg.reader().readByte());
                    break;
                }
                case -72: {
                    this.connection.bot.getMyChar().setGold(msg.reader().readInt());
                    break;
                }
                case -71: {
                    this.connection.bot.getMyChar().setGold(this.connection.bot.getMyChar().getGold() + msg.reader().readInt());
                    break;
                }
                case -77: {
                    break;
                }
                case -76: {
                    break;
                }
                case -75: {
                    this.connection.bot.getMyChar().getArrItemBox()[msg.reader().readInt()] = null;
                    break;
                }
                case -80: {
                    break;
                }
                case -91: {
                    int num9 = msg.reader().readUnsignedByte();
                    Item[] array3 = new Item[num9];
                    for (int num10 = 0; num10 < this.connection.bot.getMyChar().getArrItemBag().length; ++num10) {
                        array3[num10] = this.connection.bot.getMyChar().getArrItemBag()[num10];
                    }
                    this.connection.bot.getMyChar().setArrItemBag(array3);
                    this.connection.bot.getMyChar().getArrItemBag()[msg.reader().readUnsignedByte()] = null;
                    break;
                }
                case -87: {
                    byte index = msg.reader().readByte();
                    Party party = (Party)this.connection.bot.getParty().elementAt(index);
                    this.connection.bot.getParty().setElementAt(this.connection.bot.getParty().elementAt(0), index);
                    if (party == null) break;
                    this.connection.bot.getParty().setElementAt(party, 0);
                    break;
                }
                case -86: {
                    this.connection.bot.getParty().removeAllElements();
                    break;
                }
                case -81: {
                    this.connection.bot.getMyChar().setPk(msg.reader().readByte());
                    break;
                }
                case -85: {
                    mVector friends = this.connection.bot.getFriends();
                    friends.removeAllElements();
                    try {
                        while (true) {
                            String friendName = msg.reader().readUTF();
                            byte type = msg.reader().readByte();
                            friends.addElement(friendName);
                        }
                    }
                    catch (Exception friendName) {
                        break;
                    }
                }
                case -83: {
                    mVector friends = this.connection.bot.getFriends();
                    String friendName = msg.reader().readUTF();
                    for (int i = 0; i < friends.size(); ++i) {
                        if (!friends.elementAt(i).equals(friendName)) continue;
                        friends.removeElementAt(i);
                        break block5;
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadInfoMap(Message msg) {
        try {
            int i;
            short x = msg.reader().readShort();
            short y = msg.reader().readShort();
            this.connection.bot.getMyChar().setPosX(x);
            this.connection.bot.getMyChar().setPosY(y);
            this.connection.bot.getMyChar().setCxFirst(x);
            this.connection.bot.getMyChar().setCyFirst(y);
            int vsize = msg.reader().readByte();
            for (i = 0; i < vsize; ++i) {
                this.connection.bot.getTileMap().getvGo().addElement(new Waypoint(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort()));
            }
            vsize = msg.reader().readByte();
            for (i = 0; i < vsize; i = (int)((byte)(i + 1))) {
                Mob mob = new Mob((short)i, msg.reader().readBoolean(), msg.reader().readBoolean(), msg.reader().readBoolean(), msg.reader().readBoolean(), msg.reader().readBoolean(), msg.reader().readUnsignedByte(), msg.reader().readByte(), msg.reader().readInt(), msg.reader().readUnsignedByte(), msg.reader().readInt(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte(), msg.reader().readBoolean(), false);
                this.connection.bot.getMobs().addElement(mob);
            }
            vsize = msg.reader().readByte();
            for (i = 0; i < vsize; i = (int)((byte)(i + 1))) {
                msg.reader().readUTF();
                msg.reader().readShort();
                msg.reader().readShort();
            }
            vsize = msg.reader().readByte();
            for (i = 0; i < vsize; ++i) {
                this.connection.bot.getNpcs().addElement(new Npc(i, msg.reader().readByte(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readByte()));
            }
            vsize = msg.reader().readByte();
            for (i = 0; i < vsize; ++i) {
                ItemMap itNew = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
                boolean isHave = false;
                for (int j = 0; j < this.connection.bot.getItemsMap().size(); ++j) {
                    ItemMap itMap = (ItemMap)this.connection.bot.getItemsMap().elementAt(j);
                    if (itMap.itemMapID != itNew.itemMapID) continue;
                    isHave = true;
                    break;
                }
                if (isHave) continue;
                this.connection.bot.getItemsMap().addElement(itNew);
            }
            try {
                this.connection.bot.getTileMap().setMapName(msg.reader().readUTF());
            }
            catch (Exception i2) {
                // empty catch block
            }
            try {
                this.connection.bot.getTileMap().getLocationStand().clear();
                int size = msg.reader().readUnsignedByte();
                for (int i3 = 0; i3 < size; ++i3) {
                    int xObject = msg.reader().readUnsignedByte();
                    int yObject = msg.reader().readUnsignedByte();
                    String index = (short)(yObject * this.connection.bot.getTileMap().getTmw() + xObject) + "";
                    this.connection.bot.getTileMap().getLocationStand().put(index, "location");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            this.connection.bot.getTileMap().loadTileMap();
            this.connection.getGameScreen().partyRefreshAll();
            if (this.connection.bot.getCurrentScreen() == 0) {
                this.connection.bot.setLastOnline(Res.t());
                if (this.connection.bot.getRole() == 0) {
                    BotService.getInstance().plusNumActiveOrderBot(1);
                    this.connection.getGameScreen().orderScreen().setLastKeepBotOnline(Res.t());
                }
            }
            this.connection.bot.getWaitAction().notifyWaitMap();
            this.connection.bot.setCurrentScreen(1);
            this.connection.bot.getScreen().onOnlineEvent();
            this.connection.bot.setLoading(false);
            this.connection.bot.setTimeLoadSuccess(Res.t());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Char readCharInfo(Char chr, Message msg) {
        try {
            chr.setClanName(msg.reader().readUTF());
            if (!chr.getClanName().equals("")) {
                chr.setTypeClan(msg.reader().readByte());
            }
            chr.isInvisible = msg.reader().readBoolean();
            chr.setTypePk(msg.reader().readByte());
            chr.setNinjaClassId(msg.reader().readByte());
            chr.setGender(msg.reader().readByte());
            chr.setHead(msg.reader().readShort());
            String name = msg.reader().readUTF();
            chr.setName(name);
            chr.setNameInternal(name);
            chr.setHp(msg.reader().readInt());
            chr.setMaxHP(msg.reader().readInt());
            chr.setLevel(msg.reader().readUnsignedByte());
            msg.reader().readShort();
            msg.reader().readShort();
            msg.reader().readShort();
            msg.reader().readByte();
            short x = msg.reader().readShort();
            short y = msg.reader().readShort();
            chr.setPosX(x);
            chr.setPosY(y);
            chr.setCxFirst(x);
            chr.setCyFirst(y);
            chr.setEff5BuffHp(msg.reader().readShort());
            chr.setEff5BuffMp(msg.reader().readShort());
            this.connection.bot.getScreen().partyRefresh(chr);
            return chr;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public void addMob(Message msg) {
        try {
            byte num = msg.reader().readByte();
            for (byte b = 0; b < num; b = (byte)(b + 1)) {
                short mobId = (short)msg.reader().readUnsignedByte();
                boolean isDisable = msg.reader().readBoolean();
                boolean isDontMove = msg.reader().readBoolean();
                boolean isFire = msg.reader().readBoolean();
                boolean isIce = msg.reader().readBoolean();
                boolean isWind = msg.reader().readBoolean();
                short templateId = msg.reader().readShort();
                byte sys = msg.reader().readByte();
                int hp = msg.reader().readInt();
                int level = msg.reader().readUnsignedByte();
                int maxhp = msg.reader().readInt();
                short pointx = msg.reader().readShort();
                short pointy = msg.reader().readShort();
                byte status = msg.reader().readByte();
                byte levelBoss = msg.reader().readByte();
                boolean isBos = msg.reader().readBoolean();
                Mob mob = new Mob(mobId, isDisable, isDontMove, isFire, isIce, isWind, templateId, sys, hp, level, maxhp, pointx, pointy, status, levelBoss, isBos, true);
                this.connection.bot.getMobs().addElement(mob);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void requestItemInfo(Message msg) {
        try {
            byte num = msg.reader().readByte();
            int num2 = msg.reader().readUnsignedByte();
            Item item = null;
            switch (num) {
                case 3: {
                    item = this.connection.bot.getMyChar().getArrItemBag()[num2];
                    break;
                }
                case 4: {
                    item = this.connection.bot.getMyChar().getArrItemBox()[num2];
                    break;
                }
                case 5: {
                    item = this.connection.bot.getMyChar().getArrItemBody()[num2];
                }
            }
            item.expires = msg.reader().readLong();
            if (item.isTypeUIMe()) {
                item.saleCoinLock = msg.reader().readInt();
            }
            if (item.isTypeBody() || item.isTypeMounts() || item.isTypeNgocKham()) {
                item.sys = msg.reader().readByte();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

