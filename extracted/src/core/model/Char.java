/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.Bot;
import core.model.Item;
import lib.mVector;
import network.Message;
import utils.Res;

public class Char {
    public static byte A_STAND = 1;
    public static byte A_RUN = (byte)2;
    public static byte A_JUMP = (byte)3;
    public static byte A_FALL = (byte)4;
    public static byte A_DEADFLY = (byte)5;
    public static byte A_NOTHING = (byte)6;
    public static byte A_ATTK = (byte)7;
    public static byte A_INJURE = (byte)8;
    public static byte A_AUTOJUMP = (byte)9;
    public static byte A_WATERRUN = (byte)10;
    public static byte A_WATERDOWN = (byte)11;
    public static byte SKILL_STAND = (byte)12;
    public static byte SKILL_FALL = (byte)13;
    public static byte A_DEAD = (byte)14;
    public static byte A_HIDE = (byte)15;
    private Bot bot;
    private int charId;
    private String clanName;
    private byte typeClan;
    private byte taskId;
    private byte gender;
    private byte speed;
    private String name = "";
    private String nameInternal = "";
    private short head;
    private byte pk;
    private byte typePk;
    private int maxHP;
    private int hp;
    private int maxMP;
    private int mp;
    private long exp;
    private long expDown;
    private short eff5BuffHp;
    private short eff5BuffMp;
    private byte ninjaClassId;
    private short potentialPoint;
    private int[] potential;
    private short skillPoint;
    private int coin;
    private int kins;
    private int gold;
    private Item[] arrItemBag;
    private Item[] arrItemBody;
    private Item[] arrItemBox;
    private int posX;
    private int posY;
    private int cdx;
    private int cdy;
    private int cxFirst;
    private int cyFirst;
    public boolean isInvisible;
    private int level;
    private int statusMe;
    public int killCharId = -9999;
    private int typeActive;
    public mVector vSkill = new mVector("vSkill");
    public mVector vEff = new mVector("vEff");

    public Char() {
    }

    public Char(Bot bot) {
        this.bot = bot;
    }

    public void printInfoChar() {
        System.out.println("charID: " + this.getCharId());
        System.out.println("cName: " + this.getName());
        long[] lv = Res.getLevelExp(this.getExp());
        System.out.println("cLevel: " + lv[0] + "+" + lv[1]);
        System.out.println("HP: " + this.getHp() + "/" + this.getMaxHP());
        System.out.println("MP: " + this.getMp() + "/" + this.getMaxMP());
        System.out.println("NClassID: " + this.getNinjaClassId());
        System.out.println("cgender: " + this.getGender());
        System.out.println("cspeed: " + this.getSpeed());
        System.out.println("cClanName: " + this.getClanName());
        System.out.println("ctypeClan: " + this.getTypeClan());
        System.out.println("ctaskId: " + this.getTaskId());
        System.out.println("cPk: " + this.getPk());
        System.out.println("cTypePk: " + this.getTypePk());
        System.out.println("cExpDown: " + this.getExpDown());
        System.out.println("eff5BuffHp: " + this.getEff5BuffHp());
        System.out.println("eff5BuffMp: " + this.getEff5BuffMp());
        System.out.println("pPoint: " + this.getPotentialPoint());
        System.out.println("sPoint: " + this.getSkillPoint());
        for (int i = 0; i < this.getPotential().length; ++i) {
            System.out.println("potential[" + i + "]: " + this.getPotential()[i]);
        }
    }

    public boolean isDie() {
        return this.getHp() <= 0;
    }

    public void setLevel_Exp() {
        long[] a = Res.getLevelExp(this.getExp());
        this.setLevel((int)a[0]);
    }

    public void addParty(String name) {
        if (this.bot.getParty().isEmpty()) {
            this.bot.getConnection().getService().createParty();
            Res.sleep(500L);
        }
        this.bot.getConnection().getService().inviteIntoParty(name);
    }

    public void readParam(Message msg, String pos) {
        try {
            this.setSpeed(msg.reader().readByte());
            this.setMaxHP(msg.reader().readInt());
            this.setMaxMP(msg.reader().readInt());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitToDie(int x, int y) {
        this.posX = this.cdx = x;
        this.posY = this.cdy = y;
        this.setStatusMe(5);
        this.setHp(0);
    }

    public void liveFromDead() {
        this.setHp(this.getMaxHP());
        this.setMp(this.getMaxMP());
        this.setStatusMe(1);
    }

    public void reset() {
        this.setCharId(0);
        this.setName("");
        this.setHp(0);
        this.setMaxHP(0);
        this.setMp(0);
        this.setMaxMP(0);
    }

    public int getCharId() {
        return this.charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;
    }

    public String getClanName() {
        return this.clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public byte getTypeClan() {
        return this.typeClan;
    }

    public void setTypeClan(byte typeClan) {
        this.typeClan = typeClan;
    }

    public byte getTaskId() {
        return this.taskId;
    }

    public void setTaskId(byte taskId) {
        this.taskId = taskId;
    }

    public byte getGender() {
        return this.gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public byte getSpeed() {
        return this.speed;
    }

    public void setSpeed(byte speed) {
        this.speed = speed;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameInternal() {
        return this.nameInternal;
    }

    public void setNameInternal(String nameInternal) {
        this.nameInternal = nameInternal;
    }

    public short getHead() {
        return this.head;
    }

    public void setHead(short head) {
        this.head = head;
    }

    public byte getPk() {
        return this.pk;
    }

    public void setPk(byte pk) {
        this.pk = pk;
    }

    public byte getTypePk() {
        return this.typePk;
    }

    public void setTypePk(byte typePk) {
        this.typePk = typePk;
    }

    public int getMaxHP() {
        return this.maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxMP() {
        return this.maxMP;
    }

    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public long getExp() {
        return this.exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getExpDown() {
        return this.expDown;
    }

    public void setExpDown(long expDown) {
        this.expDown = expDown;
    }

    public short getEff5BuffHp() {
        return this.eff5BuffHp;
    }

    public void setEff5BuffHp(short eff5BuffHp) {
        this.eff5BuffHp = eff5BuffHp;
    }

    public short getEff5BuffMp() {
        return this.eff5BuffMp;
    }

    public void setEff5BuffMp(short eff5BuffMp) {
        this.eff5BuffMp = eff5BuffMp;
    }

    public byte getNinjaClassId() {
        return this.ninjaClassId;
    }

    public void setNinjaClassId(byte ninjaClassId) {
        this.ninjaClassId = ninjaClassId;
    }

    public short getPotentialPoint() {
        return this.potentialPoint;
    }

    public void setPotentialPoint(short potentialPoint) {
        this.potentialPoint = potentialPoint;
    }

    public int[] getPotential() {
        return this.potential;
    }

    public void setPotential(int[] potential) {
        this.potential = potential;
    }

    public short getSkillPoint() {
        return this.skillPoint;
    }

    public void setSkillPoint(short skillPoint) {
        this.skillPoint = skillPoint;
    }

    public int getCoin() {
        return this.coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getKins() {
        return this.kins;
    }

    public void setKins(int kins) {
        this.kins = kins;
    }

    public int getGold() {
        return this.gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public Item[] getArrItemBag() {
        return this.arrItemBag;
    }

    public void setArrItemBag(Item[] arrItemBag) {
        this.arrItemBag = arrItemBag;
    }

    public Item[] getArrItemBody() {
        return this.arrItemBody;
    }

    public void setArrItemBody(Item[] arrItemBody) {
        this.arrItemBody = arrItemBody;
    }

    public Item[] getArrItemBox() {
        return this.arrItemBox;
    }

    public void setArrItemBox(Item[] arrItemBox) {
        this.arrItemBox = arrItemBox;
    }

    public void setPos(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public int getPosX() {
        return this.posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getCdx() {
        return this.cdx;
    }

    public void setCdx(int cdx) {
        this.cdx = cdx;
    }

    public int getCdy() {
        return this.cdy;
    }

    public void setCdy(int cdy) {
        this.cdy = cdy;
    }

    public int getCxFirst() {
        return this.cxFirst;
    }

    public void setCxFirst(int cxFirst) {
        this.cxFirst = cxFirst;
    }

    public int getCyFirst() {
        return this.cyFirst;
    }

    public void setCyFirst(int cyFirst) {
        this.cyFirst = cyFirst;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStatusMe() {
        return this.statusMe;
    }

    public void setStatusMe(int statusMe) {
        this.statusMe = statusMe;
    }

    public int getTypeActive() {
        return this.typeActive;
    }

    public void setTypeActive(int typeActive) {
        this.typeActive = typeActive;
    }
}

