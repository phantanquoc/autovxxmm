/*
 * Decompiled with CFR 0.152.
 */
package core.cache;

import core.model.Skill;
import lib.mHashtable;

public class Skills {
    public static mHashtable skills = new mHashtable();

    public static void add(Skill skill) {
        skills.put(skill.skillId, skill);
    }

    public static Skill get(short skillId) {
        return (Skill)skills.get(skillId);
    }
}

