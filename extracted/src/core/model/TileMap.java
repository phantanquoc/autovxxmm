/*
 * Decompiled with CFR 0.152.
 */
package core.model;

import core.model.Bot;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import lib.mVector;
import utils.Resources;

public class TileMap {
    public static final int T_EMPTY = 0;
    public static final int T_TOP = 2;
    public static final int T_LEFT = 4;
    public static final int T_RIGHT = 8;
    public static final int T_TREE = 16;
    public static final int T_WATERFALL = 32;
    public static final int T_WATERFLOW = 64;
    public static final int T_TOPFALL = 128;
    public static final int T_OUTSIDE = 256;
    public static final int T_DOWN1PIXEL = 512;
    public static final int T_BRIDGE = 1024;
    public static final int T_UNDERWATER = 2048;
    public static final int T_SOLIDGROUND = 4096;
    public static final int T_BOTTOM = 8192;
    public static final int T_DIE = 16384;
    public static final int T_HEBI = 32768;
    public static final int T_BANG = 65536;
    public static final int T_JUM8 = 131072;
    public static final int T_NT0 = 262144;
    public static final int T_NT1 = 524288;
    public static final int T_RIVERFLOW = 0x100000;
    public static String[] mapNames = null;
    public static int size = 24;
    public static int[][][] defaultPosition = new int[7][][];
    private Bot bot;
    private short mapId;
    private byte bgID;
    private byte typeMap;
    private String mapName;
    private byte zoneId;
    private byte tileID;
    private mVector vGo = new mVector("vGo");
    private int tmw;
    private int tmh;
    private int pxw;
    private int pxh;
    private char[] maps;
    private int[] types;
    private Hashtable locationStand = new Hashtable();

    public TileMap(Bot bot) {
        this.setBot(bot);
    }

    public void loadMapFromResource() throws Exception {
        String path_map = "/map/" + this.getMapId();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Resources.read(path_map)));
        this.setTmw((char)dis.read());
        this.setTmh((char)dis.read());
        this.setMaps(new char[dis.available()]);
        for (int i = 0; i < this.getTmw() * this.getTmh(); ++i) {
            this.getMaps()[i] = (char)dis.read();
        }
        this.setTypes(new int[this.getMaps().length]);
    }

    public boolean isStand(int index) {
        if (this.getLocationStand() != null) {
            return this.getLocationStand().get(index + "") != null;
        }
        return false;
    }

    public void loadTileMap() {
        this.setPxh(this.getTmh() * size);
        this.setPxw(this.getTmw() * size);
        try {
            for (int i = 0; i < this.getTmw() * this.getTmh(); ++i) {
                if (this.isStand(i)) {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 2;
                }
                if (this.getTileID() == 4) {
                    if (this.getMaps()[i] == '\u0001' || this.getMaps()[i] == '\u0002' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\t' || this.getMaps()[i] == '\n' || this.getMaps()[i] == 'O' || this.getMaps()[i] == 'P' || this.getMaps()[i] == '\r' || this.getMaps()[i] == '\u000e' || this.getMaps()[i] == '+' || this.getMaps()[i] == ',' || this.getMaps()[i] == '-' || this.getMaps()[i] == '2') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 2;
                    }
                    if (this.getMaps()[i] == '\t' || this.getMaps()[i] == '\u000b') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 4;
                    }
                    if (this.getMaps()[i] == '\n' || this.getMaps()[i] == '\f') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 8;
                    }
                    if (this.getMaps()[i] == '\r' || this.getMaps()[i] == '\u000e') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x400;
                    }
                    if (this.getMaps()[i] == 'L' || this.getMaps()[i] == 'M') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x40;
                        if (this.getMaps()[i] == 'N') {
                            int[] nArray2 = this.getTypes();
                            int n2 = i;
                            nArray2[n2] = nArray2[n2] | 0x1000;
                        }
                    }
                }
                if (this.getTileID() == 1) {
                    if (this.getMaps()[i] == '\u0001' || this.getMaps()[i] == '\u0002' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u0007' || this.getMaps()[i] == '$' || this.getMaps()[i] == '%' || this.getMaps()[i] == '6' || this.getMaps()[i] == '[' || this.getMaps()[i] == '\\' || this.getMaps()[i] == ']' || this.getMaps()[i] == '^' || this.getMaps()[i] == 'I' || this.getMaps()[i] == 'J' || this.getMaps()[i] == 'a' || this.getMaps()[i] == 'b' || this.getMaps()[i] == 't' || this.getMaps()[i] == 'u' || this.getMaps()[i] == 'v' || this.getMaps()[i] == 'x' || this.getMaps()[i] == '=') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 2;
                    }
                    if (this.getMaps()[i] == '\u0002' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u0014' || this.getMaps()[i] == '\u0015' || this.getMaps()[i] == '\u0016' || this.getMaps()[i] == '\u0017' || this.getMaps()[i] == '$' || this.getMaps()[i] == '%' || this.getMaps()[i] == '&' || this.getMaps()[i] == '\'' || this.getMaps()[i] == '=') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x1000;
                    }
                    if (this.getMaps()[i] == '\b' || this.getMaps()[i] == '\t' || this.getMaps()[i] == '\n' || this.getMaps()[i] == '\f' || this.getMaps()[i] == '\r' || this.getMaps()[i] == '\u000e' || this.getMaps()[i] == '\u001e') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x10;
                    }
                    if (this.getMaps()[i] == '\u0011') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x20;
                    }
                    if (this.getMaps()[i] == '\u0012') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x80;
                    }
                    if (this.getMaps()[i] == '%' || this.getMaps()[i] == '&' || this.getMaps()[i] == '=') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 4;
                    }
                    if (this.getMaps()[i] == '$' || this.getMaps()[i] == '\'' || this.getMaps()[i] == '=') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 8;
                    }
                    if (this.getMaps()[i] == '\u0013') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x40;
                        if ((this.getTypes()[i - this.getTmw()] & 0x1000) == 4096) {
                            int[] nArray3 = this.getTypes();
                            int n3 = i;
                            nArray3[n3] = nArray3[n3] | 0x1000;
                        }
                    }
                    if (this.getMaps()[i] == '#') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x800;
                    }
                    if (this.getMaps()[i] == '\u0007') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x400;
                    }
                    if (this.getMaps()[i] == ' ' || this.getMaps()[i] == '!' || this.getMaps()[i] == '\"') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x100;
                    }
                }
                if (this.getTileID() == 2) {
                    if (this.getMaps()[i] == '\u0001' || this.getMaps()[i] == '\u0002' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u0007' || this.getMaps()[i] == '$' || this.getMaps()[i] == '%' || this.getMaps()[i] == '6' || this.getMaps()[i] == '=' || this.getMaps()[i] == 'I' || this.getMaps()[i] == 'L' || this.getMaps()[i] == 'M' || this.getMaps()[i] == 'N' || this.getMaps()[i] == 'O' || this.getMaps()[i] == 'R' || this.getMaps()[i] == 'S' || this.getMaps()[i] == 'b' || this.getMaps()[i] == 'c' || this.getMaps()[i] == 'd' || this.getMaps()[i] == 'f' || this.getMaps()[i] == 'g' || this.getMaps()[i] == 'l' || this.getMaps()[i] == 'm' || this.getMaps()[i] == 'n' || this.getMaps()[i] == 'p' || this.getMaps()[i] == 'q' || this.getMaps()[i] == 't' || this.getMaps()[i] == 'u' || this.getMaps()[i] == '}' || this.getMaps()[i] == '~' || this.getMaps()[i] == '\u007f' || this.getMaps()[i] == '\u0081' || this.getMaps()[i] == '\u0082') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 2;
                    }
                    if (this.getMaps()[i] == '\u0001' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u0014' || this.getMaps()[i] == '\u0015' || this.getMaps()[i] == '\u0016' || this.getMaps()[i] == '\u0017' || this.getMaps()[i] == '$' || this.getMaps()[i] == '%' || this.getMaps()[i] == '&' || this.getMaps()[i] == '\'' || this.getMaps()[i] == '7' || this.getMaps()[i] == 'm' || this.getMaps()[i] == 'o' || this.getMaps()[i] == 'p' || this.getMaps()[i] == 'q' || this.getMaps()[i] == 'r' || this.getMaps()[i] == 's' || this.getMaps()[i] == 't' || this.getMaps()[i] == '\u007f' || this.getMaps()[i] == '\u0081' || this.getMaps()[i] == '\u0082') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x1000;
                    }
                    if (this.getMaps()[i] == '\b' || this.getMaps()[i] == '\t' || this.getMaps()[i] == '\n' || this.getMaps()[i] == '\f' || this.getMaps()[i] == '\r' || this.getMaps()[i] == '\u000e' || this.getMaps()[i] == '\u001e' || this.getMaps()[i] == '\u0087') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x10;
                    }
                    if (this.getMaps()[i] == '\u0011') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x20;
                    }
                    if (this.getMaps()[i] == '\u0012') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x80;
                    }
                    if (this.getMaps()[i] == '=' || this.getMaps()[i] == '%' || this.getMaps()[i] == '&' || this.getMaps()[i] == '\u007f' || this.getMaps()[i] == '\u0082' || this.getMaps()[i] == '\u0083') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 4;
                    }
                    if (this.getMaps()[i] == '=' || this.getMaps()[i] == '$' || this.getMaps()[i] == '\'' || this.getMaps()[i] == '\u007f' || this.getMaps()[i] == '\u0081' || this.getMaps()[i] == '\u0084') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 8;
                    }
                    if (this.getMaps()[i] == '\u0013') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x40;
                        if ((this.getTypes()[i - this.getTmw()] & 0x1000) == 4096) {
                            int[] nArray4 = this.getTypes();
                            int n4 = i;
                            nArray4[n4] = nArray4[n4] | 0x1000;
                        }
                    }
                    if (this.getMaps()[i] == '\u0086') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x40;
                        if ((this.getTypes()[i - this.getTmw()] & 0x1000) == 4096) {
                            int[] nArray5 = this.getTypes();
                            int n5 = i;
                            nArray5[n5] = nArray5[n5] | 0x1000;
                        }
                    }
                    if (this.getMaps()[i] == '#') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x800;
                    }
                    if (this.getMaps()[i] == '\u0007') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x400;
                    }
                    if (this.getMaps()[i] == ' ' || this.getMaps()[i] == '!' || this.getMaps()[i] == '\"') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x100;
                    }
                    if (this.getMaps()[i] == '=' || this.getMaps()[i] == '\u007f') {
                        int[] nArray = this.getTypes();
                        int n = i;
                        nArray[n] = nArray[n] | 0x2000;
                    }
                }
                if (this.getTileID() != 3) continue;
                if (this.getMaps()[i] == '\u0001' || this.getMaps()[i] == '\u0002' || this.getMaps()[i] == '\u0003' || this.getMaps()[i] == '\u0004' || this.getMaps()[i] == '\u0005' || this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u0007' || this.getMaps()[i] == '\u000b' || this.getMaps()[i] == '\u000e' || this.getMaps()[i] == '\u0011' || this.getMaps()[i] == '+' || this.getMaps()[i] == '3' || this.getMaps()[i] == '?' || this.getMaps()[i] == 'A' || this.getMaps()[i] == 'C' || this.getMaps()[i] == 'D' || this.getMaps()[i] == 'G' || this.getMaps()[i] == 'H' || this.getMaps()[i] == 'S' || this.getMaps()[i] == 'T' || this.getMaps()[i] == 'U' || this.getMaps()[i] == 'W' || this.getMaps()[i] == '[' || this.getMaps()[i] == '^' || this.getMaps()[i] == 'a' || this.getMaps()[i] == 'b' || this.getMaps()[i] == 'j' || this.getMaps()[i] == 'k' || this.getMaps()[i] == 'o' || this.getMaps()[i] == 'q' || this.getMaps()[i] == 'u' || this.getMaps()[i] == 'v' || this.getMaps()[i] == 'w' || this.getMaps()[i] == '}' || this.getMaps()[i] == '~' || this.getMaps()[i] == '\u0081' || this.getMaps()[i] == '\u0082' || this.getMaps()[i] == '\u0083' || this.getMaps()[i] == '\u0085' || this.getMaps()[i] == '\u0088' || this.getMaps()[i] == '\u008a' || this.getMaps()[i] == '\u008b' || this.getMaps()[i] == '\u008e') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 2;
                }
                if (this.getMaps()[i] == '|' || this.getMaps()[i] == 't' || this.getMaps()[i] == '{' || this.getMaps()[i] == ',' || this.getMaps()[i] == '\f' || this.getMaps()[i] == '\u000f' || this.getMaps()[i] == '\u0010' || this.getMaps()[i] == '-' || this.getMaps()[i] == '\n' || this.getMaps()[i] == '\t') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x1000;
                }
                if (this.getMaps()[i] == '\u0017') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x20;
                }
                if (this.getMaps()[i] == '\u0018') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x80;
                }
                if (this.getMaps()[i] == '\u0006' || this.getMaps()[i] == '\u000f' || this.getMaps()[i] == '3' || this.getMaps()[i] == '_' || this.getMaps()[i] == 'a' || this.getMaps()[i] == 'j' || this.getMaps()[i] == 'o' || this.getMaps()[i] == '{' || this.getMaps()[i] == '}' || this.getMaps()[i] == '\u008a' || this.getMaps()[i] == '\u008c') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 4;
                }
                if (this.getMaps()[i] == '\u0007' || this.getMaps()[i] == '\u0010' || this.getMaps()[i] == '3' || this.getMaps()[i] == '`' || this.getMaps()[i] == 'b' || this.getMaps()[i] == 'k' || this.getMaps()[i] == 'o' || this.getMaps()[i] == '|' || this.getMaps()[i] == '~' || this.getMaps()[i] == '\u008b' || this.getMaps()[i] == '\u008d') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 8;
                }
                if (this.getMaps()[i] == '\u0019') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x40;
                    if ((this.getTypes()[i - this.getTmw()] & 0x1000) == 4096) {
                        int[] nArray6 = this.getTypes();
                        int n6 = i;
                        nArray6[n6] = nArray6[n6] | 0x1000;
                    }
                }
                if (this.getMaps()[i] == '\"') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x800;
                }
                if (this.getMaps()[i] == '\u0011') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x400;
                }
                if (this.getMaps()[i] == '!' || this.getMaps()[i] == 'g' || this.getMaps()[i] == 'h' || this.getMaps()[i] == 'i' || this.getMaps()[i] == '\u001a' || this.getMaps()[i] == '!') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x100;
                }
                if (this.getMaps()[i] == '3' || this.getMaps()[i] == 'o' || this.getMaps()[i] == 'D') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x2000;
                }
                if (this.getMaps()[i] == 'R' || this.getMaps()[i] == 'n' || this.getMaps()[i] == '\u008f') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x4000;
                }
                if (this.getMaps()[i] == 'q') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x10000;
                }
                if (this.getMaps()[i] == '\u008e') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x8000;
                }
                if (this.getMaps()[i] == '(' || this.getMaps()[i] == ')') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x20000;
                }
                if (this.getMaps()[i] == 'n') {
                    int[] nArray = this.getTypes();
                    int n = i;
                    nArray[n] = nArray[n] | 0x40000;
                }
                if (this.getMaps()[i] != '\u008f') continue;
                int[] nArray = this.getTypes();
                int n = i;
                nArray[n] = nArray[n] | 0x80000;
            }
        }
        catch (Exception e) {
            System.out.println("Error Load Map");
            e.printStackTrace();
        }
    }

    public final int tileAt(int x, int y) {
        try {
            return this.getMaps()[y * this.getTmw() + x];
        }
        catch (Exception ex) {
            return 1000;
        }
    }

    public final int tileTypeAt(int x, int y) {
        try {
            return this.getTypes()[y * this.getTmw() + x];
        }
        catch (Exception ex) {
            return 1000;
        }
    }

    public final int tileTypeAtPixel(int px, int py) {
        try {
            return this.getTypes()[py / size * this.getTmw() + px / size];
        }
        catch (Exception ex) {
            return 1000;
        }
    }

    public final boolean tileTypeAt(int px, int py, int t) {
        try {
            return (this.getTypes()[py / size * this.getTmw() + px / size] & t) == t;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public final void setTileTypeAtPixel(int px, int py, int t) {
        int[] nArray = this.getTypes();
        int n = py / size * this.getTmw() + px / size;
        nArray[n] = nArray[n] | t;
    }

    public final void setTileTypeAt(int x, int y, int t) {
        this.getTypes()[y * this.getTmw() + x] = t;
    }

    public final void killTileTypeAt(int px, int py, int t) {
        int[] nArray = this.getTypes();
        int n = py / size * this.getTmw() + px / size;
        nArray[n] = nArray[n] & ~t;
    }

    public final int tileYofPixel(int py) {
        return py / size * size;
    }

    public final int tileXofPixel(int px) {
        return px / size * size;
    }

    public int getIndexX(int x, int y) {
        if ((this.tileTypeAtPixel(x - 16, y) & 0x4002) != 0) {
            int i2;
            int i;
            x = this.tileXofPixel(x);
            for (i = 24; i < 240; i += 24) {
                i2 = this.tileTypeAtPixel(x - i, y);
                if (x - i <= 0 || (i2 & 0x4002) != 0) continue;
                return x - i + 24;
            }
            for (i = 24; i < 120; i += 24) {
                i2 = this.tileTypeAtPixel(x + i, y);
                if (x + i >= this.getPxw() || (i2 & 0x4002) != 0) continue;
                return x + i;
            }
        }
        return x;
    }

    public int getIndexY(int x, int y) {
        if ((this.tileTypeAtPixel(x, y - 16) & 0x4002) != 0) {
            int i2;
            int i;
            y = this.tileYofPixel(y);
            for (i = 24; i < 240; i += 24) {
                i2 = this.tileTypeAtPixel(x, y - i);
                if (y - i <= 0 || (i2 & 0x4002) != 0) continue;
                return y - i + 24;
            }
            for (i = 24; i < 120; i += 24) {
                i2 = this.tileTypeAtPixel(x, y + i);
                if (y + i >= this.getPxh() || (i2 & 0x4002) != 0) continue;
                return y + i;
            }
        }
        return y;
    }

    public static int[] getPosition(int mapID, int pID) {
        int id = 0;
        if (mapID == 10) {
            id = 0;
        } else if (mapID == 17) {
            id = 1;
        } else if (mapID == 22) {
            id = 2;
        } else if (mapID == 32) {
            id = 3;
        } else if (mapID == 38) {
            id = 4;
        } else if (mapID == 43) {
            id = 5;
        } else if (mapID == 48) {
            id = 6;
        }
        if (pID < 0 || pID >= 3) {
            pID = 0;
        }
        return defaultPosition[id][pID];
    }

    public void reset() {
        this.setMapId((short)0);
        this.setMapName("");
        this.setZoneId((byte)0);
    }

    public Bot getBot() {
        return this.bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public short getMapId() {
        return this.mapId;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public byte getBgID() {
        return this.bgID;
    }

    public void setBgID(byte bgID) {
        this.bgID = bgID;
    }

    public byte getTypeMap() {
        return this.typeMap;
    }

    public void setTypeMap(byte typeMap) {
        this.typeMap = typeMap;
    }

    public String getMapName() {
        return this.mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public byte getZoneId() {
        return this.zoneId;
    }

    public void setZoneId(byte zoneId) {
        this.zoneId = zoneId;
    }

    public byte getTileID() {
        return this.tileID;
    }

    public void setTileID(byte tileID) {
        this.tileID = tileID;
    }

    public mVector getvGo() {
        return this.vGo;
    }

    public void setvGo(mVector vGo) {
        this.vGo = vGo;
    }

    public int getTmw() {
        return this.tmw;
    }

    public void setTmw(int tmw) {
        this.tmw = tmw;
    }

    public int getTmh() {
        return this.tmh;
    }

    public void setTmh(int tmh) {
        this.tmh = tmh;
    }

    public int getPxw() {
        return this.pxw;
    }

    public void setPxw(int pxw) {
        this.pxw = pxw;
    }

    public int getPxh() {
        return this.pxh;
    }

    public void setPxh(int pxh) {
        this.pxh = pxh;
    }

    public char[] getMaps() {
        return this.maps;
    }

    public void setMaps(char[] maps) {
        this.maps = maps;
    }

    public int[] getTypes() {
        return this.types;
    }

    public void setTypes(int[] types) {
        this.types = types;
    }

    public Hashtable getLocationStand() {
        return this.locationStand;
    }

    public void setLocationStand(Hashtable locationStand) {
        this.locationStand = locationStand;
    }

    static {
        TileMap.defaultPosition[0] = new int[][]{{179, 192}, {307, 216}, {461, 216}};
        TileMap.defaultPosition[1] = new int[][]{{1549, 192}, {1267, 240}, {575, 288}};
        TileMap.defaultPosition[2] = new int[][]{{423, 144}, {387, 216}, {205, 168}};
        TileMap.defaultPosition[3] = new int[][]{{2681, 456}, {2521, 456}, {2539, 384}};
        TileMap.defaultPosition[4] = new int[][]{{428, 240}, {330, 336}, {505, 336}};
        TileMap.defaultPosition[5] = new int[][]{{2407, 288}, {2523, 384}, {2287, 384}};
        TileMap.defaultPosition[6] = new int[][]{{286, 264}, {314, 360}, {572, 264}};
    }
}

