/*
 * Decompiled with CFR 0.152.
 */
package core.module;

import core.model.Bot;
import core.model.Char;
import core.model.Mob;
import core.model.Npc;
import core.model.Party;
import core.module.impl.CollectScreen;
import core.module.impl.OrderScreen;
import core.module.impl.TransferScreen;
import java.awt.image.BufferedImage;
import lib.mVector;
import main.Application;
import service.SettingService;
import utils.Res;

public abstract class GameScreen
implements Runnable {
    public static final int MAX_PARTY = 6;
    private static final long UPDATE_PARTY_INTERVAL = 1000L;
    public Bot bot;
    public boolean onThread;
    private long lastReturnTown;
    private long lastSendPlayerAttack;
    private long lastJump;
    public boolean isCaptcha;
    private long lastTimeUpdateParty;
    private long lastChangeZone;
    public boolean isChangeMap = false;
    public int mapChange;
    public boolean isChangeZone = false;
    public int zoneChange;
    private long lastRequestFriends;
    public String[] maStr = new String[]{"LEFT", "UP", "RIGHT"};
    protected int[] maARGB = new int[600];

    public GameScreen(Bot bot) {
        this.bot = bot;
        this.onThread = true;
        Thread t = new Thread((Runnable)this, "GameScreen [" + bot.getAccount() + "]");
        t.start();
    }

    @Override
    public final void run() {
        while (this.onThread) {
            Res.sleep(100L);
            try {
                if (Application.systemInterrupt) continue;
                this.bot.getObserver().observe();
                if (!this.bot.isOnline()) continue;
                if (this.bot.getMyChar().isDie()) {
                    this.returnTown();
                    continue;
                }
                this.updateFriends();
                this.autoJump();
                this.updateAccountProtect();
                if (Res.t() - this.lastTimeUpdateParty > 1000L) {
                    this.updateParty();
                    this.lastTimeUpdateParty = Res.t();
                }
                this.onAliveActivities();
                if (!this.isChangeMap || !this.bot.isMapValid()) continue;
                this.isChangeMap = false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void onAliveActivities();

    public void onOnlineEvent() {
    }

    public void onOfflineEvent() {
    }

    public void onDieEvent() {
    }

    private void returnTown() {
        if (Res.t() - this.lastReturnTown > 5000L) {
            this.bot.getConnection().getService().returnTownFromDead();
            this.lastReturnTown = Res.t();
        }
    }

    private void updateAccountProtect() {
        if (SettingService.getInstance().isAutoRegisterProtection() && SettingService.getInstance().isValidProtectionCode() && this.bot.getMyChar().getTypeActive() == 0 && this.bot.getMyChar().getKins() >= 10000) {
            this.bot.getConnection().getService().activeAccProtect(Integer.parseInt(SettingService.getInstance().getProtectionCode()));
        }
        if (SettingService.getInstance().isAutoOpenProtection() && SettingService.getInstance().isValidProtectionCode() && this.bot.getMyChar().getTypeActive() == 1) {
            this.bot.getConnection().getService().openLockAccProtect(Integer.parseInt(SettingService.getInstance().getProtectionCode()));
        }
    }

    public void updateParty() {
    }

    public void handleServerDialogMessage(String text) {
        try {
            String[] tx;
            if (text.equals("B\u1ea1n \u0111\u00e3 g\u1edfi y\u00eau c\u1ea7u giao d\u1ecbch. Sau 30 gi\u00e2y n\u1eefa m\u1edbi \u0111\u01b0\u1ee3c g\u1edfi ti\u1ebfp")) {
                this.bot.getTrade().reset();
                return;
            }
            int second = 0;
            if (text.startsWith("B\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 v\u00e0o l\u1ea1i game sau")) {
                tx = new String[]{"B\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 v\u00e0o l\u1ea1i game sau ", " gi\u00e2y n\u1eefa"};
                second = Integer.parseInt(text.substring(tx[0].length(), text.indexOf(tx[1])));
            }
            if (text.startsWith("T\u00e0i kho\u1ea3n c\u1ee7a b\u1ea1n \u0111ang b\u1ecb kho\u00e1 do s\u1eed d\u1ee5ng phi\u00ean b\u1ea3n mod g\u00e2y lag m\u00e1y ch\u1ee7. B\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 \u0111\u0103ng nh\u1eadp l\u1ea1i sau ")) {
                tx = new String[]{"T\u00e0i kho\u1ea3n c\u1ee7a b\u1ea1n \u0111ang b\u1ecb kho\u00e1 do s\u1eed d\u1ee5ng phi\u00ean b\u1ea3n mod g\u00e2y lag m\u00e1y ch\u1ee7. B\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 \u0111\u0103ng nh\u1eadp l\u1ea1i sau ", "s"};
                second = Integer.parseInt(text.substring(tx[0].length(), text.lastIndexOf(115)));
            }
            if (text.startsWith("C\u00f3 l\u1ed7i x\u1ea3y ra")) {
                second = 5;
            }
            if (text.startsWith("C\u00f3 ng\u01b0\u1eddi kh\u00e1c \u0111\u0103ng nh\u1eadp")) {
                second = 30;
            }
            if (text.startsWith("T\u00e0i kho\u1ea3n n\u00e0y \u0111ang b\u1ecb kh\u00f3a")) {
                second = 99999;
                if (SettingService.getInstance().isProtectBot()) {
                    Application.alert("T\u00e0i kho\u1ea3n " + this.bot.getAccount() + " \u0111\u00e3 b\u1ecb kh\u00f3a. \u0110\u1ec3 b\u1ea3o v\u1ec7 c\u00e1c t\u00e0i kho\u1ea3n kh\u00e1c, h\u1ec7 th\u1ed1ng \u0111\u00e3 t\u1ef1 \u0111\u1ed9ng ng\u1eaft k\u1ebft n\u1ed1i t\u1ea5t c\u1ea3 c\u00e1c t\u00e0i kho\u1ea3n \u0111\u1ec3 b\u1ea3o v\u1ec7 s\u1ef1 an to\u00e0n. H\u00e3y nh\u1ea5n n\u00fat [OK] \u0111\u1ec3 c\u00e1c t\u00e0i kho\u1ea3n online tr\u1edf l\u1ea1i!");
                }
            }
            if (second > 0) {
                this.bot.getAutoLogin().startReconnect(second);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleServerAlertMessage(String text) {
    }

    public void handleServerMessage(String text) {
    }

    public void handleRequestInviteIntoParty(String name, int id) {
    }

    public void handleRequestJoinParty(String name) {
    }

    public void partyRefreshAll() {
        for (int i = 0; i < this.bot.getParty().size(); ++i) {
            Party party = (Party)this.bot.getParty().elementAt(i);
            if (party.charId == this.bot.getMyChar().getCharId()) continue;
            party.c = this.findCharInMap(party.charId);
        }
    }

    public void partyRefresh(Char c) {
        for (int i = 0; i < this.bot.getParty().size(); ++i) {
            Party party = (Party)this.bot.getParty().elementAt(i);
            if (party.charId != c.getCharId()) continue;
            party.c = c;
            break;
        }
    }

    public void partyClear(int charId) {
        for (int i = 0; i < this.bot.getParty().size(); ++i) {
            Party party = (Party)this.bot.getParty().elementAt(i);
            if (party.charId != charId) continue;
            party.c = null;
            break;
        }
    }

    protected boolean compareMapAndZone(int map, int zone) {
        return this.bot.getTileMap().getMapId() == map && this.bot.getTileMap().getZoneId() == zone;
    }

    public void changeMapAndZone(int map, int zone) {
        if (!this.compareMap(map)) {
            this.changeMap(map);
        } else if (!this.compareZone(zone)) {
            this.changeZone(zone);
        }
    }

    public boolean sameParty(String name) {
        mVector party = this.bot.getParty();
        return !party.isEmpty() && party.stream().anyMatch(p -> ((Party)p).name.equals(name));
    }

    private boolean compareMap(int map) {
        return this.bot.getTileMap().getMapId() == map;
    }

    protected void changeMap(int map) {
        if (this.bot.getTileMap().getMapId() != map) {
            this.mapChange = map;
            this.isChangeMap = true;
            this.bot.getNextMap().gotoMap(map);
            if (this.isChangeMap && this.bot.getTileMap().getMapId() == map) {
                this.mapChange = -1;
                this.isChangeMap = false;
            }
        }
    }

    private boolean compareZone(int zone) {
        return this.bot.getTileMap().getZoneId() == zone;
    }

    protected void changeZone(int zoneId) {
        if (this.bot.getTileMap().getZoneId() != zoneId && Res.t() - this.lastChangeZone >= 10000L) {
            this.zoneChange = zoneId;
            this.isChangeZone = true;
            Npc npc = this.findNearNpcInMap(13);
            if (npc == null) {
                this.lastChangeZone = Res.t();
                return;
            }
            this.move(npc.getPosX(), npc.getPosY());
            Res.sleep(2000L);
            this.bot.getConnection().getService().requestChangeZone(zoneId, -1);
            this.bot.getWaitAction().waitMap();
            Res.sleep(2000L);
            this.lastChangeZone = Res.t();
            if (this.isChangeZone && this.bot.getTileMap().getZoneId() == zoneId) {
                this.zoneChange = -1;
                this.isChangeZone = false;
            }
        }
    }

    public void move(int x, int y) {
        int toY;
        Char mc = this.bot.getMyChar();
        if (x == mc.getPosX() && y == mc.getPosY()) {
            return;
        }
        int range = 24;
        int curX = mc.getPosX();
        int n = toY = this.bot.getTileMap().tileTypeAt(x, y - 1, 64) ? this.bot.getTileMap().tileYofPixel(y) - 24 : y;
        while (true) {
            int newY;
            if (curX < x) {
                if ((curX += range) > x) {
                    curX = x;
                }
                newY = this.bot.getTileMap().getIndexY(curX, toY);
                this.bot.getConnection().getService().charMove(curX, newY);
                mc.setPos(curX, newY);
            } else {
                if (curX <= x) break;
                if ((curX -= range) < x) {
                    curX = x;
                }
                newY = this.bot.getTileMap().getIndexY(curX, toY);
                this.bot.getConnection().getService().charMove(curX, newY);
                mc.setPos(curX, newY);
            }
            Res.sleep(150L);
        }
        this.bot.getConnection().getService().charMove(x, y);
        mc.setPos(x, y);
    }

    public Char findCharInMap(int charID) {
        for (int i = 0; i < this.bot.getCharsInMap().size(); ++i) {
            Char chr = (Char)this.bot.getCharsInMap().elementAt(i);
            if (charID != chr.getCharId()) continue;
            return chr;
        }
        return null;
    }

    public Char findCharInMap(String name) {
        for (int i = 0; i < this.bot.getCharsInMap().size(); ++i) {
            Char chr = (Char)this.bot.getCharsInMap().elementAt(i);
            if (!chr.getName().equals(name)) continue;
            return chr;
        }
        return null;
    }

    public void removeCharInMap(int charID) {
        for (int i = 0; i < this.bot.getCharsInMap().size(); ++i) {
            Char chr = (Char)this.bot.getCharsInMap().elementAt(i);
            if (charID != chr.getCharId()) continue;
            this.bot.getCharsInMap().removeElementAt(i);
            return;
        }
    }

    public Mob getMob(int id) {
        for (int i = 0; i < this.bot.getMobs().size(); ++i) {
            Mob m = (Mob)this.bot.getMobs().elementAt(i);
            if (m == null || m.mobId != id) continue;
            return m;
        }
        return null;
    }

    public Npc findNpcInMap(int npcId) {
        for (int i = 0; i < this.bot.getNpcs().size(); ++i) {
            Npc npc = (Npc)this.bot.getNpcs().elementAt(i);
            if (npc == null || npc.template.npcId != npcId) continue;
            return npc;
        }
        return null;
    }

    public Npc findNearNpcInMap(int npcId) {
        Npc near = null;
        for (int i = 0; i < this.bot.getNpcs().size(); ++i) {
            Npc npc = (Npc)this.bot.getNpcs().elementAt(i);
            if (npc == null || npc.template.npcId != npcId || near != null && !(Res.distance(this.bot.getMyChar().getPosX(), this.bot.getMyChar().getPosY(), npc.getPosX(), npc.getPosY()) < Res.distance(this.bot.getMyChar().getPosX(), this.bot.getMyChar().getPosY(), near.getPosX(), near.getPosY()))) continue;
            near = npc;
        }
        return near;
    }

    public Npc findNpcNextMap() {
        Npc near = null;
        for (int i = 0; i < this.bot.getNpcs().size(); ++i) {
            Npc npc = (Npc)this.bot.getNpcs().elementAt(i);
            if (npc == null || npc.template.npcId != 7 && npc.template.npcId != 8 || near != null && (near == null || !(Res.distance(this.bot.getMyChar().getPosX(), this.bot.getMyChar().getPosY(), npc.getPosX(), npc.getPosY()) < Res.distance(this.bot.getMyChar().getPosX(), this.bot.getMyChar().getPosY(), near.getPosX(), near.getPosY())))) continue;
            near = npc;
        }
        return near;
    }

    public Mob findFirstMobAlive() {
        mVector mobs = this.bot.getMobs();
        for (int i = 0; i < mobs.size(); ++i) {
            Mob m = (Mob)mobs.elementAt(i);
            if (m == null || m.status == 0 || m.hp <= 0 || m.isBoss) continue;
            return m;
        }
        return null;
    }

    public void parseGhost(BufferedImage image) {
        image.getRGB(0, 15, 60, 10, this.maARGB, 0, 60);
        new Thread(new Runnable(){

            @Override
            public void run() {
                for (int i = 0; i < 6; ++i) {
                    byte ma;
                    Res.sleep(1000L);
                    if (GameScreen.this.maARGB[i * 10 + 120] >>> 24 != 0) {
                        ma = 2;
                    } else {
                        int j;
                        ma = 0;
                        byte ma2 = 0;
                        for (j = 0; j < 10; ++j) {
                            if (GameScreen.this.maARGB[i * 10 + j] >>> 24 == 0) continue;
                            ma2 = (byte)(ma2 + 1);
                        }
                        for (j = 0; j < 10; ++j) {
                            if (GameScreen.this.maARGB[i * 10 + j * 60] >>> 24 == 0) continue;
                            ma = (byte)(ma + 1);
                        }
                        ma = ma2 < ma ? (byte)1 : 0;
                    }
                    GameScreen.this.bot.getConnection().getService().send_Captcha(ma);
                    GameScreen.this.bot.getConnection().debug("Gi\u1ea3i captcha: " + GameScreen.this.maStr[ma]);
                }
            }
        }).start();
    }

    private void autoJump() {
        if (Res.t() - this.lastJump > 5000L) {
            int cx = this.bot.getMyChar().getPosX();
            int cy = this.bot.getMyChar().getPosY();
            this.bot.getConnection().getService().charMove(cx, cy - 12);
            this.bot.getConnection().getService().charMove(cx, cy);
            this.lastJump = Res.t();
        }
    }

    private void updateFriends() {
        mVector friends;
        if (Res.t() - this.lastRequestFriends > 10000L) {
            this.bot.getConnection().getService().requestFriends();
            this.lastRequestFriends = Res.t();
        }
        if ((friends = this.bot.getFriends()).isEmpty()) {
            return;
        }
        for (int i = 0; i < friends.size(); ++i) {
            String name = (String)friends.elementAt(i);
            this.bot.getConnection().getService().removeFriend(name);
            friends.removeElementAt(i--);
            this.bot.debug("\u0110\u00e3 x\u00f3a " + name + " kh\u1ecfi danh s\u00e1ch b\u1ea1n b\u00e8");
        }
    }

    public void dumpToDie() {
        this.move(this.bot.getMyChar().getPosX(), this.bot.getTileMap().getPxh());
        this.bot.getWaitAction().waitDie();
    }

    public String getStatusName() {
        return "";
    }

    public OrderScreen orderScreen() {
        return (OrderScreen)this;
    }

    public CollectScreen collectScreen() {
        return (CollectScreen)this;
    }

    public TransferScreen transferScreen() {
        return (TransferScreen)this;
    }
}

