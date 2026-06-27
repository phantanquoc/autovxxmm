/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.Item;
import core.template.ItemTemplate;

public class ItemMap {
    public short itemMapID;
    public ItemTemplate template;
    public int x;
    public int y;
    public int xEnd;
    public int yEnd;
    public int status;

    public ItemMap(short itemMapID, short itemTemplateID, int x, int y) {
        this.itemMapID = itemMapID;
        this.template = Item.get(itemTemplateID);
        this.x = this.xEnd = x;
        this.y = this.yEnd = y;
        this.status = 1;
    }
}

