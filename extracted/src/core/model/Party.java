/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.Bot;
import core.model.Char;
import utils.Res;

public class Party {
    public Bot bot;
    public int charId;
    public int level;
    public byte classId;
    public short iconId;
    public String name;
    public boolean isLock;
    public Char c;
    public int size;
    public long lastTimeParty = Res.t();

    public Party(Bot bot, byte classId, int level, String name, int size) {
        this.bot = bot;
        this.classId = classId;
        switch (classId) {
            case 0: {
                this.iconId = (short)647;
                break;
            }
            case 1: {
                this.iconId = (short)1182;
                break;
            }
            case 2: {
                this.iconId = (short)1181;
                break;
            }
            case 3: {
                this.iconId = (short)643;
                break;
            }
            case 4: {
                this.iconId = (short)645;
                break;
            }
            case 5: {
                this.iconId = (short)676;
                break;
            }
            case 6: {
                this.iconId = (short)1119;
            }
        }
        this.name = name;
        this.level = level;
        this.size = size;
    }

    public Party(Bot bot, int charId, byte classId, String name, boolean isLock) {
        this.bot = bot;
        this.charId = charId;
        this.isLock = isLock;
        switch (classId) {
            case 0: {
                this.iconId = (short)647;
                break;
            }
            case 1: {
                this.iconId = (short)1182;
                break;
            }
            case 2: {
                this.iconId = (short)1181;
                break;
            }
            case 3: {
                this.iconId = (short)643;
                break;
            }
            case 4: {
                this.iconId = (short)645;
                break;
            }
            case 5: {
                this.iconId = (short)676;
                break;
            }
            case 6: {
                this.iconId = (short)1119;
            }
        }
        this.name = name;
        this.c = charId == bot.getMyChar().getCharId() ? bot.getMyChar() : bot.getScreen().findCharInMap(charId);
    }
}

