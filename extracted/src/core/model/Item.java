/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.template.ItemTemplate;
import java.util.Hashtable;

public class Item {
    public static final int TYPE_BODY_MIN = 0;
    public static final int TYPE_BODY_MAX = 15;
    public static final int TYPE_NON = 0;
    public static final int TYPE_VUKHI = 1;
    public static final int TYPE_AO = 2;
    public static final int TYPE_LIEN = 3;
    public static final int TYPE_GANGTAY = 4;
    public static final int TYPE_NHAN = 5;
    public static final int TYPE_QUAN = 6;
    public static final int TYPE_NGOCBOI = 7;
    public static final int TYPE_GIAY = 8;
    public static final int TYPE_PHU = 9;
    public static final int TYPE_THUNUOI = 10;
    public static final int TYPE_MATNA = 11;
    public static final int TYPE_AOCHOANG = 12;
    public static final int TYPE_BAOTAY = 13;
    public static final int TYPE_UNKNOW2 = 14;
    public static final int TYPE_BIKIP = 15;
    public static final int TYPE_HP = 16;
    public static final int TYPE_MP = 17;
    public static final int TYPE_EAT = 18;
    public static final int TYPE_MONEY = 19;
    public static final int TYPE_TUI_TIEN = 20;
    public static final int TYPE_MEAT = 21;
    public static final int TYPE_DRAGONBALL = 22;
    public static final int TYPE_TASK_SAVE = 23;
    public static final int TYPE_TASK_WAIT = 24;
    public static final int TYPE_TASK = 25;
    public static final int TYPE_CRYSTAL = 26;
    public static final int TYPE_ORDER = 27;
    public static final int TYPE_PROTECT = 28;
    public static final int TYPE_MON0 = 29;
    public static final int TYPE_MON1 = 30;
    public static final int TYPE_MON2 = 31;
    public static final int TYPE_MON3 = 32;
    public static final int TYPE_MON4 = 33;
    public static final int TYPE_NGOCKHAM = 34;
    public static final byte UI_WEAPON = 2;
    public static final byte UI_BAG = 3;
    public static final byte UI_BOX = 4;
    public static final byte UI_BODY = 5;
    public static final byte UI_STACK = 6;
    public static final byte UI_STACK_LOCK = 7;
    public static final byte UI_GROCERY = 8;
    public static final byte UI_GROCERY_LOCK = 9;
    public static final byte UI_UPGRADE = 10;
    public static final byte UI_UPPEARL = 11;
    public static final byte UI_UPPEARL_LOCK = 12;
    public static final byte UI_SPLIT = 13;
    public static final byte UI_STORE = 14;
    public static final byte UI_BOOK = 15;
    public static final byte UI_LIEN = 16;
    public static final byte UI_NHAN = 17;
    public static final byte UI_NGOCBOI = 18;
    public static final byte UI_PHU = 19;
    public static final byte UI_NONNAM = 20;
    public static final byte UI_NONNU = 21;
    public static final byte UI_AONAM = 22;
    public static final byte UI_AONU = 23;
    public static final byte UI_GANGTAYNAM = 24;
    public static final byte UI_GANGTAYNU = 25;
    public static final byte UI_QUANNAM = 26;
    public static final byte UI_QUANNU = 27;
    public static final byte UI_GIAYNAM = 28;
    public static final byte UI_GIAYNU = 29;
    public static final byte UI_TRADE = 30;
    public static final byte UI_UPGRADE_GOLD = 31;
    public static final byte UI_FASHION = 32;
    public static final byte UI_CONVERT = 33;
    public static final byte UI_CLANSHOP = 34;
    public static final byte UI_ELITES = 35;
    public static final byte UI_AUCTION_SALE = 36;
    public static final byte UI_AUCTION_BUY = 37;
    public static final byte UI_LUCKY_SPIN = 38;
    public static final byte UI_CLAN = 39;
    public static final byte UI_AUTO = 40;
    public static final byte UI_MON = 41;
    public static final byte UI_NAP_GOOGLE = 42;
    public static final byte UI_LUYEN_THACH = 43;
    public static final byte UI_TINH_LUYEN_AO = 44;
    public static final byte UI_TINH_LUYEN_THU = 45;
    public static Hashtable itemTemps = new Hashtable();
    public ItemTemplate template;
    public int itemId;
    public int playerId;
    public int indexUI;
    public int quantity;
    public long expires;
    public boolean isLock;
    public int sys;
    public int upgrade;
    public int buyCoin;
    public int buyCoinLock;
    public int buyGold;
    public int buyGoldLock;
    public int saleCoinLock;
    public int typeUI;
    public boolean isExpires;
    public int indexEff;

    public static void add(ItemTemplate item) {
        itemTemps.put(new Short(item.id), item);
    }

    public static ItemTemplate get(short id) {
        return (ItemTemplate)itemTemps.get(new Short(id));
    }

    public boolean isTypeUIMe() {
        return this.typeUI == 5 || this.typeUI == 3 || this.typeUI == 4 || this.typeUI == 39;
    }

    public boolean isTypeBody() {
        return 0 <= this.template.type && this.template.type <= 15;
    }

    public boolean isTypeNgocKham() {
        return this.template.type == 34;
    }

    public boolean isTypeMounts() {
        return 29 <= this.template.type && this.template.type <= 33;
    }
}

