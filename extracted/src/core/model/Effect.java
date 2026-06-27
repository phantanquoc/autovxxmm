/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.template.EffectTemplate;

public class Effect {
    public static EffectTemplate[] effTemplates;
    public int timeStart;
    public int timeLenght;
    public short param;
    public EffectTemplate template;

    public Effect(byte templateId, int timeStart, int timeLenght, short param) {
        this.template = effTemplates[templateId];
        this.timeStart = timeStart;
        this.timeLenght = timeLenght / 1000;
        this.param = param;
    }
}

