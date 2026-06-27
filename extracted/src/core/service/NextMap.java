/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import core.model.Npc;
import core.model.Waypoint;
import core.module.GameScreen;
import java.util.Vector;
import utils.Res;

public class NextMap {
    private static int[][] wayPoint = null;
    private static int[][] wayMap = null;
    private static int infinity;
    private Bot bot;
    private boolean[] tick = new boolean[160];
    private int[] wp = new int[160];
    private int[] mwp = new int[160];

    public NextMap(Bot bot) {
        this.bot = bot;
    }

    private static int[][] processArray(int[][] input) {
        int size = input.length;
        int[][] output = new int[size][size];
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < input[i].length; ++j) {
                output[i][input[i][j]] = 1;
            }
        }
        return output;
    }

    public static boolean isVillage(int mapID) {
        return mapID == 10 || mapID == 17 || mapID == 22 || mapID == 32 || mapID == 38 || mapID == 43 || mapID == 48 || mapID == 138;
    }

    public static boolean isLangCo(int mapID) {
        return mapID >= 134 && mapID <= 138;
    }

    public static boolean isSchool(int mapID) {
        return mapID == 1 || mapID == 27 || mapID == 72;
    }

    public void gotoMap(int mapID) {
        try {
            Vector<Integer> ways = this.getListMap(this.bot.getTileMap().getMapId(), mapID);
            if (ways.size() >= 2) {
                this.doNextMap(ways);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void doNextMap(Vector<Integer> ways) throws Exception {
        for (int i = 0; i < ways.size() - 1; ++i) {
            int currMap = ways.elementAt(i);
            int nextMap = ways.elementAt(i + 1);
            if (currMap != this.bot.getTileMap().getMapId()) continue;
            if (NextMap.isVillage(currMap) && NextMap.isVillage(nextMap)) {
                if (this.moveToVillage(nextMap)) continue;
                return;
            }
            if (NextMap.isSchool(currMap) && NextMap.isSchool(nextMap)) {
                if (this.moveToSchool(nextMap)) continue;
                return;
            }
            if (NextMap.isVillage(currMap) && nextMap == 139) {
                if (this.moveToDevilLand()) continue;
                return;
            }
            int hole = -1;
            for (int j = 0; j < wayPoint[currMap].length; ++j) {
                if (wayPoint[currMap][j] != nextMap) continue;
                hole = j;
            }
            if (hole == -1) {
                return;
            }
            this.moveToHole(hole);
        }
        Res.sleep(5000L);
    }

    private boolean moveToVillage(int mapID) {
        int id = -1;
        if (mapID == 10) {
            id = 1;
        } else if (mapID == 17) {
            id = 2;
        } else if (mapID == 22) {
            id = 3;
        } else if (mapID == 32) {
            id = 4;
        } else if (mapID == 38) {
            id = 5;
        } else if (mapID == 43) {
            id = 6;
        } else if (mapID == 48) {
            id = 7;
        }
        if (id == -1) {
            return false;
        }
        Npc nearNpc = null;
        nearNpc = this.gameScr().findNpcNextMap();
        if (nearNpc == null) {
            System.out.println("NPC IS NULL");
            return false;
        }
        this.gameScr().move(nearNpc.getPosX(), nearNpc.getPosY());
        Res.sleep(2000L);
        this.bot.getConnection().getService().menu(nearNpc.template.npcId, id, 0);
        this.bot.getConnection().bot.getWaitAction().waitMap();
        Res.sleep(5000L);
        return true;
    }

    private boolean moveToSchool(int mapID) {
        int id = -1;
        if (mapID == 1) {
            id = 0;
        } else if (mapID == 27) {
            id = 1;
        } else if (mapID == 72) {
            id = 2;
        }
        if (id == -1) {
            return false;
        }
        Npc nearNpc = null;
        nearNpc = this.gameScr().findNpcNextMap();
        if (nearNpc == null) {
            return false;
        }
        this.gameScr().move(nearNpc.getPosX(), nearNpc.getPosY());
        Res.sleep(2000L);
        this.bot.getConnection().getService().menu(nearNpc.template.npcId, id, 0);
        this.bot.getConnection().bot.getWaitAction().waitMap();
        Res.sleep(5000L);
        return true;
    }

    private boolean moveToDevilLand() {
        Npc nearNpc = null;
        nearNpc = this.gameScr().findNpcInMap(5);
        if (nearNpc == null) {
            return false;
        }
        this.gameScr().move(nearNpc.getPosX(), nearNpc.getPosY());
        Res.sleep(2000L);
        this.bot.getConnection().getService().menu(nearNpc.template.npcId, 2, 0);
        Res.sleep(5000L);
        return true;
    }

    public void moveToHole(int hole) {
        int size = this.bot.getTileMap().getvGo().size();
        if (hole < 0 || hole >= size) {
            throw new RuntimeException("Hole not found");
        }
        Waypoint wp = (Waypoint)this.bot.getTileMap().getvGo().elementAt(hole);
        int mx = wp.minX;
        int my = wp.minY;
        if (wp.minY != 0 && wp.maxY < this.bot.getTileMap().getPxh() - 24) {
            if (wp.maxX <= this.bot.getTileMap().getPxw() / 2) {
                mx = wp.maxX + 12;
                my = wp.maxY;
            } else if (wp.minX >= this.bot.getTileMap().getPxw() / 2) {
                mx = wp.minX - 12;
                my = wp.maxY;
            }
        } else if (wp.maxY <= this.bot.getTileMap().getPxh() / 2) {
            mx = (wp.maxX + wp.minX) / 2;
            my = wp.maxY + 24;
        } else if (wp.minY >= this.bot.getTileMap().getPxh() / 2) {
            mx = (wp.maxX + wp.minX) / 2 + 24;
            my = wp.maxY - 48;
        }
        if (my < 0) {
            my = 0;
        }
        this.gameScr().move(mx, my);
        Res.sleep(2000L);
        this.bot.getConnection().getService().requestChangeMap();
        this.bot.getConnection().bot.getWaitAction().waitMap();
        Res.sleep(2000L);
    }

    private Vector<Integer> getListMap(int from, int to) {
        int i;
        int n = wayMap.length;
        int start = from;
        int finish = to;
        int istart = start;
        for (int i2 = 0; i2 < n; ++i2) {
            this.tick[i2] = false;
            this.mwp[i2] = infinity;
            this.wp[i2] = -1;
        }
        this.wp[istart] = 0;
        this.mwp[istart] = 0;
        while (istart != finish) {
            int min = infinity + 1;
            for (i = 0; i < n; ++i) {
                if (this.tick[i] || min <= this.mwp[i]) continue;
                min = this.mwp[i];
                istart = i;
            }
            this.tick[istart] = true;
            if (istart == finish) continue;
            for (i = 0; i < n; ++i) {
                if (wayMap[istart][i] <= 0 || this.mwp[istart] + wayMap[istart][i] >= this.mwp[i]) continue;
                this.mwp[i] = this.mwp[istart] + wayMap[istart][i];
                this.wp[i] = istart;
            }
        }
        Vector<Integer> way = new Vector<Integer>();
        i = finish;
        while (i != start) {
            way.insertElementAt(i, 0);
            i = this.wp[i];
        }
        way.insertElementAt(start, 0);
        return way;
    }

    private GameScreen gameScr() {
        return this.bot.getScreen();
    }

    static {
        if (wayPoint == null) {
            wayPoint = new int[160][];
            NextMap.wayPoint[0] = new int[]{27};
            NextMap.wayPoint[1] = new int[]{2, 3, 27, 72};
            NextMap.wayPoint[2] = new int[]{6, 1};
            NextMap.wayPoint[3] = new int[]{1, 4};
            NextMap.wayPoint[4] = new int[]{3, 5};
            NextMap.wayPoint[5] = new int[]{7, 4};
            NextMap.wayPoint[6] = new int[]{7, 2, 20, 21};
            NextMap.wayPoint[7] = new int[]{6, 5, 8};
            NextMap.wayPoint[8] = new int[]{7, 9};
            NextMap.wayPoint[9] = new int[]{8, 10};
            NextMap.wayPoint[10] = new int[]{9, 11, 17, 22, 32, 38, 43, 48, 139};
            NextMap.wayPoint[11] = new int[]{12, 10};
            NextMap.wayPoint[12] = new int[]{11, 57};
            NextMap.wayPoint[13] = new int[]{57, 14};
            NextMap.wayPoint[14] = new int[]{13, 15};
            NextMap.wayPoint[15] = new int[]{14, 16};
            NextMap.wayPoint[16] = new int[]{15, 17};
            NextMap.wayPoint[17] = new int[]{16, 18, 10, 22, 32, 38, 43, 48, 139};
            NextMap.wayPoint[18] = new int[]{17, 19};
            NextMap.wayPoint[19] = new int[]{18, 58};
            NextMap.wayPoint[20] = new int[]{6};
            NextMap.wayPoint[21] = new int[]{22, 6};
            NextMap.wayPoint[22] = new int[]{23, 21, 10, 17, 32, 38, 43, 48, 139};
            NextMap.wayPoint[23] = new int[]{22, 69, 25};
            NextMap.wayPoint[24] = new int[]{59, 36};
            NextMap.wayPoint[25] = new int[]{23, 26};
            NextMap.wayPoint[26] = new int[]{27, 25};
            NextMap.wayPoint[27] = new int[]{26, 28, 1, 72};
            NextMap.wayPoint[28] = new int[]{27, 60};
            NextMap.wayPoint[29] = new int[]{60, 30};
            NextMap.wayPoint[30] = new int[]{29, 31};
            NextMap.wayPoint[31] = new int[]{32, 30};
            NextMap.wayPoint[32] = new int[]{31, 61, 10, 17, 22, 38, 43, 48, 139};
            NextMap.wayPoint[33] = new int[]{61, 34};
            NextMap.wayPoint[34] = new int[]{35, 33};
            NextMap.wayPoint[35] = new int[]{34, 66};
            NextMap.wayPoint[36] = new int[]{37, 24};
            NextMap.wayPoint[37] = new int[]{36};
            NextMap.wayPoint[38] = new int[]{67, 68, 10, 17, 22, 32, 43, 48, 139};
            NextMap.wayPoint[39] = new int[]{72, 46, 40};
            NextMap.wayPoint[40] = new int[]{39, 65, 41};
            NextMap.wayPoint[41] = new int[]{42, 40, 43};
            NextMap.wayPoint[42] = new int[]{62, 41};
            NextMap.wayPoint[43] = new int[]{41, 44, 10, 17, 22, 32, 38, 48, 139};
            NextMap.wayPoint[44] = new int[]{43, 45};
            NextMap.wayPoint[45] = new int[]{44, 53};
            NextMap.wayPoint[46] = new int[]{63, 39, 47};
            NextMap.wayPoint[47] = new int[]{46, 48};
            NextMap.wayPoint[48] = new int[]{47, 50, 10, 17, 22, 32, 38, 43, 139};
            NextMap.wayPoint[49] = new int[]{50, 51};
            NextMap.wayPoint[50] = new int[]{48, 49};
            NextMap.wayPoint[51] = new int[]{52, 49};
            NextMap.wayPoint[52] = new int[]{51, 64};
            NextMap.wayPoint[53] = new int[]{54, 45};
            NextMap.wayPoint[54] = new int[]{55, 53};
            NextMap.wayPoint[55] = new int[]{54};
            NextMap.wayPoint[56] = new int[]{72};
            NextMap.wayPoint[57] = new int[]{12, 13};
            NextMap.wayPoint[58] = new int[]{19};
            NextMap.wayPoint[59] = new int[]{68, 24};
            NextMap.wayPoint[60] = new int[]{28, 29};
            NextMap.wayPoint[61] = new int[]{33, 32};
            NextMap.wayPoint[62] = new int[]{42};
            NextMap.wayPoint[63] = new int[]{46};
            NextMap.wayPoint[64] = new int[]{52};
            NextMap.wayPoint[65] = new int[]{40};
            NextMap.wayPoint[66] = new int[]{67, 35};
            NextMap.wayPoint[67] = new int[]{66, 38};
            NextMap.wayPoint[68] = new int[]{59, 38};
            NextMap.wayPoint[69] = new int[]{70, 23};
            NextMap.wayPoint[70] = new int[]{69, 71};
            NextMap.wayPoint[71] = new int[]{72, 70};
            NextMap.wayPoint[72] = new int[]{71, 39, 1, 27};
            NextMap.wayPoint[73] = new int[]{1};
            NextMap.wayPoint[74] = new int[0];
            NextMap.wayPoint[75] = new int[0];
            NextMap.wayPoint[76] = new int[0];
            NextMap.wayPoint[77] = new int[0];
            NextMap.wayPoint[78] = new int[0];
            NextMap.wayPoint[79] = new int[0];
            NextMap.wayPoint[80] = new int[]{81, 82, 83};
            NextMap.wayPoint[81] = new int[]{80, 84};
            NextMap.wayPoint[82] = new int[]{80, 85};
            NextMap.wayPoint[83] = new int[]{80, 86};
            NextMap.wayPoint[84] = new int[]{81, 87};
            NextMap.wayPoint[85] = new int[]{82, 88};
            NextMap.wayPoint[86] = new int[]{83, 89};
            NextMap.wayPoint[87] = new int[]{84, 90};
            NextMap.wayPoint[88] = new int[]{85, 90};
            NextMap.wayPoint[89] = new int[]{86, 90};
            NextMap.wayPoint[90] = new int[0];
            NextMap.wayPoint[91] = new int[]{92};
            NextMap.wayPoint[92] = new int[]{91, 93};
            NextMap.wayPoint[93] = new int[]{92};
            NextMap.wayPoint[94] = new int[]{95};
            NextMap.wayPoint[95] = new int[]{94, 96};
            NextMap.wayPoint[96] = new int[]{95, 97};
            NextMap.wayPoint[97] = new int[]{96};
            NextMap.wayPoint[98] = new int[]{99};
            NextMap.wayPoint[99] = new int[]{98, 101, 100, 102};
            NextMap.wayPoint[100] = new int[]{99, 103};
            NextMap.wayPoint[101] = new int[]{99, 103};
            NextMap.wayPoint[102] = new int[]{99, 103};
            NextMap.wayPoint[103] = new int[]{101, 102, 104, 100};
            NextMap.wayPoint[104] = new int[]{103};
            NextMap.wayPoint[105] = new int[]{107, 106, 108};
            NextMap.wayPoint[106] = new int[]{105, 109};
            NextMap.wayPoint[107] = new int[]{105, 109};
            NextMap.wayPoint[108] = new int[]{105, 109};
            NextMap.wayPoint[109] = new int[]{106, 107, 108};
            NextMap.wayPoint[110] = new int[0];
            NextMap.wayPoint[111] = new int[0];
            NextMap.wayPoint[112] = new int[]{113};
            NextMap.wayPoint[113] = new int[]{112};
            NextMap.wayPoint[114] = new int[]{115};
            NextMap.wayPoint[115] = new int[]{114, 116};
            NextMap.wayPoint[116] = new int[]{115};
            NextMap.wayPoint[117] = new int[0];
            NextMap.wayPoint[118] = new int[0];
            NextMap.wayPoint[119] = new int[0];
            NextMap.wayPoint[120] = new int[0];
            NextMap.wayPoint[121] = new int[0];
            NextMap.wayPoint[122] = new int[0];
            NextMap.wayPoint[123] = new int[0];
            NextMap.wayPoint[124] = new int[0];
            NextMap.wayPoint[125] = new int[]{126};
            NextMap.wayPoint[126] = new int[]{125, 127};
            NextMap.wayPoint[127] = new int[]{126, 128};
            NextMap.wayPoint[128] = new int[]{127};
            NextMap.wayPoint[129] = new int[0];
            NextMap.wayPoint[130] = new int[0];
            NextMap.wayPoint[131] = new int[0];
            NextMap.wayPoint[132] = new int[0];
            NextMap.wayPoint[133] = new int[0];
            NextMap.wayPoint[134] = new int[]{138};
            NextMap.wayPoint[135] = new int[]{138};
            NextMap.wayPoint[136] = new int[]{138};
            NextMap.wayPoint[137] = new int[]{138};
            NextMap.wayPoint[138] = new int[]{134, 135, 136, 137};
            NextMap.wayPoint[139] = new int[]{140};
            NextMap.wayPoint[140] = new int[]{139, 141};
            NextMap.wayPoint[141] = new int[]{140, 142};
            NextMap.wayPoint[142] = new int[]{141, 143};
            NextMap.wayPoint[143] = new int[]{142, 144};
            NextMap.wayPoint[144] = new int[]{143, 145};
            NextMap.wayPoint[145] = new int[]{144, 146};
            NextMap.wayPoint[146] = new int[]{145, 147};
            NextMap.wayPoint[147] = new int[]{146, 148};
            NextMap.wayPoint[148] = new int[]{147};
            NextMap.wayPoint[149] = new int[0];
            NextMap.wayPoint[150] = new int[0];
            NextMap.wayPoint[151] = new int[0];
            NextMap.wayPoint[152] = new int[0];
            NextMap.wayPoint[153] = new int[0];
            NextMap.wayPoint[154] = new int[0];
            NextMap.wayPoint[155] = new int[0];
            NextMap.wayPoint[156] = new int[0];
            NextMap.wayPoint[157] = new int[]{158, 159};
            NextMap.wayPoint[158] = new int[]{157, 159};
            NextMap.wayPoint[159] = new int[]{158, 157};
            wayMap = NextMap.processArray(wayPoint);
            infinity = 0;
            for (int i = 0; i < wayPoint.length; ++i) {
                for (int j = 0; j < wayPoint.length; ++j) {
                    infinity += wayMap[i][j];
                }
            }
        }
    }
}

