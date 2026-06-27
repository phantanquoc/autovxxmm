/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.SkillOption;
import core.template.SkillTemplate;

public class Skill {
    public final byte ATT_STAND = 0;
    public final byte ATT_FLY = 1;
    public final byte SKILL_AUTO_USE = 0;
    public final byte SKILL_CLICK_USE_ATTACK = 1;
    public final byte SKILL_CLICK_USE_BUFF = (byte)2;
    public final byte SKILL_CLICK_NPC = (byte)3;
    public final byte SKILL_CLICK_LIVE = (byte)4;
    public SkillTemplate template;
    public short skillId;
    public int point;
    public int level;
    public int coolDown;
    public long lastTimeUseThisSkill;
    public int dx;
    public int dy;
    public int maxFight;
    public int manaUse;
    public SkillOption[] options;
    public boolean paintCanNotUseSkill;
}

