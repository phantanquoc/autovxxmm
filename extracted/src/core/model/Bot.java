/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package core.model;

import com.google.gson.JsonObject;
import core.model.BotObserver;
import core.model.BotStatus;
import core.model.Char;
import core.model.Order;
import core.model.Party;
import core.model.Server;
import core.model.TileMap;
import core.model.WaitAction;
import core.module.GameScreen;
import core.module.Trade;
import core.module.impl.CollectScreen;
import core.module.impl.CollectTrade;
import core.module.impl.OrderScreen;
import core.module.impl.OrderTrade;
import core.module.impl.TransferScreen;
import core.module.impl.TransferTrade;
import core.service.AutoLogin;
import core.service.HeartBeat;
import core.service.LuckyDraw;
import core.service.MessageStream;
import core.service.NextMap;
import lib.mVector;
import network.Connection;
import service.ServerService;
import utils.LocalLog;
import utils.Res;

public class Bot {
    public static final int TYPE_LUCKY_VIP = 0;
    public static final int TYPE_LUCKY_NOMRAL = 1;
    public static final int ROLE_ORDER = 0;
    public static final int ROLE_TRANSFER = 1;
    public static final int ROLE_COLLECT = 2;
    public static final int OFFLINE_SCREEN = 0;
    public static final int ONLINE_SCREEN = 1;
    private int role;
    private int id;
    private Server server;
    private String account;
    private String password;
    private String charName;
    private int mapId;
    private int zoneId;
    private int posX;
    private int posY;
    private String manager;
    private String[] chat;
    private String[] sms;
    private boolean enable = true;
    private int playFee;
    private byte typeLuckyDraw;
    private BotObserver observer;
    private long lastOnline;
    private boolean loading;
    private long timeLoadSuccess = Res.t();
    private int currentScreen = 0;
    private String playerName;
    private Connection connection;
    private WaitAction waitAction;
    private GameScreen screen;
    private AutoLogin autoLogin;
    private Char myChar;
    private TileMap tileMap;
    private NextMap nextMap;
    private Trade trade;
    private LuckyDraw luckyDraw;
    private MessageStream messageStream;
    private LocalLog localLog;
    private HeartBeat heartBeat;
    private mVector mobs = new mVector("vMob");
    private mVector charsInMap = new mVector("vCharInMap");
    private mVector itemsMap = new mVector("vItemMap");
    private mVector npcs = new mVector("vNpc");
    private mVector party = new mVector("vParty");
    private mVector friends = new mVector("vFriend");

    private Bot(int role) {
        this.role = role;
    }

    public static Bot createBotOrder(JsonObject botJson) {
        Bot bot = new Bot(0);
        bot.setId(botJson.get("id").getAsInt());
        bot.setAccount(botJson.get("account").getAsString());
        bot.setPassword(botJson.get("password").getAsString());
        bot.setCharName(botJson.get("charName").getAsString());
        bot.setServer(ServerService.getInstance().getServer(botJson.get("serverId").getAsInt()));
        bot.setMapId(botJson.get("mapId").getAsInt());
        bot.setZoneId(botJson.get("zoneId").getAsInt());
        bot.setPosX(botJson.get("posX").getAsInt());
        bot.setPosY(botJson.get("posY").getAsInt());
        bot.setManager(botJson.get("manager").getAsString().trim());
        bot.setChat(Res.split(botJson.get("chat").getAsString(), ";"));
        bot.setSms(Res.split(botJson.get("sms").getAsString(), ";"));
        bot.setEnable(botJson.get("enable").getAsBoolean());
        bot.setPlayFee(botJson.get("playFee").getAsInt());
        bot.setTypeLuckyDraw(botJson.get("typeLuckyDraw").getAsByte());
        OrderScreen gameScreen = new OrderScreen(bot);
        OrderTrade trade = new OrderTrade(bot, gameScreen);
        bot.setScreen(gameScreen);
        bot.setTrade(trade);
        bot.setMessageStream(new MessageStream(bot, gameScreen, trade));
        bot.setObserver(new BotObserver(bot));
        bot.createConnect();
        return bot;
    }

    public static Bot createBotCollect(JsonObject botJson) {
        Bot bot = new Bot(2);
        bot.setId(botJson.get("id").getAsInt());
        bot.setAccount(botJson.get("account").getAsString());
        bot.setPassword(botJson.get("password").getAsString());
        bot.setCharName(botJson.get("charName").getAsString());
        bot.setServer(ServerService.getInstance().getServer(botJson.get("serverId").getAsInt()));
        bot.setMapId(botJson.get("mapId").getAsInt());
        bot.setZoneId(botJson.get("zoneId").getAsInt());
        bot.setPosX(botJson.get("posX").getAsInt());
        bot.setPosY(botJson.get("posY").getAsInt());
        bot.setManager(botJson.get("manager").getAsString().trim());
        bot.setChat(Res.split(botJson.get("chat").getAsString(), ";"));
        bot.setSms(Res.split(botJson.get("sms").getAsString(), ";"));
        bot.setEnable(botJson.get("enable").getAsBoolean());
        CollectScreen screen = new CollectScreen(bot);
        CollectTrade trade = new CollectTrade(bot, screen);
        bot.setScreen(screen);
        bot.setTrade(trade);
        bot.setObserver(new BotObserver(bot));
        bot.createConnect();
        return bot;
    }

    public static Bot createBotTransfer(int id, Server server, String account, String password, String charName) {
        Bot bot = new Bot(1);
        bot.setId(id);
        bot.setServer(server);
        bot.setAccount(account);
        bot.setPassword(password);
        bot.setCharName(charName);
        TransferScreen screen = new TransferScreen(bot);
        TransferTrade trade = new TransferTrade(bot, screen);
        bot.setScreen(screen);
        bot.setTrade(trade);
        return bot;
    }

    public void destroy() {
        this.getConnection().cleanNetwork();
        this.getAutoLogin().onThread = false;
        this.getScreen().onThread = false;
        this.getHeartBeat().onThread = false;
        if (this.getMessageStream() != null) {
            this.getMessageStream().onThread = false;
        }
        System.gc();
    }

    public void createConnect() {
        this.connection = new Connection(this);
        this.waitAction = new WaitAction();
        this.autoLogin = new AutoLogin(this);
        this.heartBeat = new HeartBeat(this);
        this.myChar = new Char(this);
        this.tileMap = new TileMap(this);
        this.nextMap = new NextMap(this);
        this.luckyDraw = new LuckyDraw(this);
        this.localLog = new LocalLog(this);
        this.autoLogin.power(true);
    }

    public int getOrderStatus() {
        OrderScreen screen = this.getScreen().orderScreen();
        Order order = screen.getOrder();
        if (order == null) {
            return -1;
        }
        if (order.hasStatus(0)) {
            return 0;
        }
        if (order.hasStatus(1)) {
            return 1;
        }
        if (order.hasStatus(3)) {
            return 2;
        }
        return -1;
    }

    public void removeAllVector(boolean all) {
        this.getMobs().removeAllElements();
        this.getCharsInMap().removeAllElements();
        this.getItemsMap().removeAllElements();
        this.getNpcs().removeAllElements();
        if (all) {
            this.getParty().removeAllElements();
            this.getTrade().reset();
            this.friends.removeAllElements();
            // COLLECT bots are created without a MessageStream (see createBotCollect).
            if (this.messageStream != null) {
                this.messageStream.clearLastParty();
            }
        }
        try {
            this.getTileMap().getvGo().removeAllElements();
            this.getTileMap().getLocationStand().clear();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.messageStream != null) {
            this.messageStream.clearAllPrivateMessages();
        }
    }

    public String getServerIP() {
        return this.getServer().getIp();
    }

    public int getServerPort() {
        return this.getServer().getPort();
    }

    public byte getServerType() {
        return this.getServer().getType();
    }

    public String getNameServer() {
        return this.getServer().getName();
    }

    public boolean isOnline() {
        return this.currentScreen == 1 && this.getMyChar() != null && !this.getMyChar().getName().isEmpty();
    }

    public boolean isMapValid() {
        return this.tileMap.getMapId() == this.mapId && this.tileMap.getZoneId() == this.zoneId;
    }

    public BotStatus getStatus() {
        if (this.getCurrentScreen() == 1) {
            return BotStatus.ONLINE;
        }
        if (!this.isEnable()) {
            return BotStatus.OFFLINE;
        }
        return BotStatus.CONNECTING;
    }

    private String getRoleName() {
        switch (this.role) {
            case 0: {
                return "\u0111\u1eb7t thu\u00ea";
            }
            case 1: {
                return "b\u01a1m xu";
            }
            case 2: {
                return "gom xu";
            }
        }
        return "";
    }

    public void debug(String message) {
        System.out.println(this.myChar.getName() + "[" + this.account + " - " + this.getRoleName() + "]: " + message);
    }

    public void debug(Exception e) {
        System.out.println("==================================================");
        System.out.println(this.myChar.getName() + "[" + this.account + " - " + this.getRoleName() + "]: ");
        e.printStackTrace(System.out);
        System.out.println("==================================================");
    }

    public boolean isLeaderOfParty() {
        return !this.party.isEmpty() && ((Party)this.party.firstElement()).name.equals(this.myChar.getName());
    }

    public int getRole() {
        return this.role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCharName() {
        return this.charName;
    }

    public void setCharName(String charName) {
        this.charName = charName;
    }

    public int getMapId() {
        return this.mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getZoneId() {
        return this.zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public int getPosX() {
        return this.posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public String getManager() {
        return this.manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String[] getChat() {
        return this.chat;
    }

    public void setChat(String[] chat) {
        this.chat = chat;
    }

    public String[] getSms() {
        return this.sms;
    }

    public void setSms(String[] sms) {
        this.sms = sms;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getPlayFee() {
        return this.playFee;
    }

    public void setPlayFee(int playFee) {
        this.playFee = playFee;
    }

    public byte getTypeLuckyDraw() {
        return this.typeLuckyDraw;
    }

    public void setTypeLuckyDraw(byte typeLuckyDraw) {
        this.typeLuckyDraw = typeLuckyDraw;
    }

    public void setObserver(BotObserver observer) {
        this.observer = observer;
    }

    public long getLastOnline() {
        return this.lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public int getCurrentScreen() {
        return this.currentScreen;
    }

    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public WaitAction getWaitAction() {
        return this.waitAction;
    }

    public void setWaitAction(WaitAction waitAction) {
        this.waitAction = waitAction;
    }

    public GameScreen getScreen() {
        return this.screen;
    }

    public void setScreen(GameScreen screen) {
        this.screen = screen;
    }

    public AutoLogin getAutoLogin() {
        return this.autoLogin;
    }

    public void setAutoLogin(AutoLogin autoLogin) {
        this.autoLogin = autoLogin;
    }

    public Char getMyChar() {
        return this.myChar;
    }

    public void setMyChar(Char myChar) {
        this.myChar = myChar;
    }

    public TileMap getTileMap() {
        return this.tileMap;
    }

    public void setTileMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public NextMap getNextMap() {
        return this.nextMap;
    }

    public void setNextMap(NextMap nextMap) {
        this.nextMap = nextMap;
    }

    public Trade getTrade() {
        return this.trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public LuckyDraw getLuckyDraw() {
        return this.luckyDraw;
    }

    public void setLuckyDraw(LuckyDraw luckyDraw) {
        this.luckyDraw = luckyDraw;
    }

    public MessageStream getMessageStream() {
        return this.messageStream;
    }

    public void setMessageStream(MessageStream messageStream) {
        this.messageStream = messageStream;
    }

    public LocalLog getLocalLog() {
        return this.localLog;
    }

    public void setrData(LocalLog localLog) {
        this.localLog = localLog;
    }

    public mVector getMobs() {
        return this.mobs;
    }

    public void setMobs(mVector mobs) {
        this.mobs = mobs;
    }

    public mVector getCharsInMap() {
        return this.charsInMap;
    }

    public void setCharsInMap(mVector charsInMap) {
        this.charsInMap = charsInMap;
    }

    public mVector getItemsMap() {
        return this.itemsMap;
    }

    public void setItemsMap(mVector itemsMap) {
        this.itemsMap = itemsMap;
    }

    public mVector getNpcs() {
        return this.npcs;
    }

    public void setNpcs(mVector npcs) {
        this.npcs = npcs;
    }

    public mVector getParty() {
        return this.party;
    }

    public void setParty(mVector party) {
        this.party = party;
    }

    public long getTimeLoadSuccess() {
        return this.timeLoadSuccess;
    }

    public void setTimeLoadSuccess(long timeLoadSuccess) {
        this.timeLoadSuccess = timeLoadSuccess;
    }

    public BotObserver getObserver() {
        return this.observer;
    }

    public HeartBeat getHeartBeat() {
        return this.heartBeat;
    }

    public void setHeartBeat(HeartBeat heartBeat) {
        this.heartBeat = heartBeat;
    }

    public mVector getFriends() {
        return this.friends;
    }
}

