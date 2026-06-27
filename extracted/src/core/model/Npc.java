/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.Char;
import core.template.NpcTemplate;

public class Npc
extends Char {
    public static NpcTemplate[] arrNpcTemplate;
    public NpcTemplate template;
    public int npcId;
    public boolean isFocus = true;
    public int sys;

    public Npc(int npcId, int status, int cx, int cy, int templateId) {
        this.npcId = npcId;
        this.setPosX(cx);
        this.setPosY(cy);
        this.setStatusMe(status);
        this.template = arrNpcTemplate[templateId];
    }
}

