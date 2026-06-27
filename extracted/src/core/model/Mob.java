/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.template.MobTemplate;

public class Mob {
    public static MobTemplate[] arrMobTemplate;
    public boolean isDisable;
    public boolean isDontMove;
    public boolean isFire;
    public boolean isIce;
    public boolean isWind;
    public int sys;
    public short mobId;
    public int templateId;
    public int hp;
    public short level;
    public int x;
    public int y;
    public int maxHp;
    public byte status;
    public byte levelBoss;
    public boolean isBoss;

    public Mob(short mobId, boolean isDisable, boolean isDontMove, boolean isFire, boolean isIce, boolean isWind, int templateId, int sys, int hp, int level, int maxp, short pointx, short pointy, byte status, byte levelBoss, boolean isBos, boolean removeWhenDie) {
        this.isDisable = isDisable;
        this.isDontMove = isDontMove;
        this.isFire = isFire;
        this.isIce = isIce;
        this.isWind = isWind;
        this.sys = sys;
        this.mobId = mobId;
        this.templateId = templateId;
        this.hp = hp;
        this.level = (short)level;
        this.x = pointx;
        this.y = pointy;
        this.maxHp = maxp;
        this.status = status;
        this.levelBoss = levelBoss;
        this.isBoss = isBos;
    }

    public MobTemplate getTemplate() {
        return arrMobTemplate[this.templateId];
    }
}

