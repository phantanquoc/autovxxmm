/*
 * Decompiled with CFR 0.152.
 */
package core.template;

public class ItemTemplate {
    public short id;
    public byte type;
    public byte gender;
    public String name;
    public String description;
    public byte level;
    public short iconID;
    public short part;
    public boolean isUpToUp;
    public int w;
    public int h;

    public ItemTemplate(short templateID, byte type, byte gender, String name, String description, byte level, short iconID, short part, boolean isUpToUp) {
        this.id = templateID;
        this.type = type;
        this.gender = gender;
        this.name = name;
        this.description = description;
        this.level = level;
        this.iconID = iconID;
        this.part = part;
        this.isUpToUp = isUpToUp;
    }
}

