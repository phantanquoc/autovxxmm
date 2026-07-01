/*
 * Decompiled with CFR 0.152.
 */
package service;

import core.model.Bot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import service.BotUpdaterService;

public class BotService {
    public static final int COMPARE_ALL_ROLE = 0;
    public static final int COMPARE_ORDER_ROLE = 1;
    public static final int COMPARE_TRANSFER_ROLE = 2;
    public static final int COMPARE_COLLECT_ROLE = 3;
    public static final int COMPARE_TRANSFER_COLLECT_ROLE = 4;
    private static final BotService instance = new BotService();
    private BotUpdaterService updaterService;
    private final List<Bot> botOrders = new ArrayList<Bot>();
    private final Map<LocationIndex, List<Bot>> botOrdersByLocation = new HashMap<LocationIndex, List<Bot>>();
    private final List<Bot> botTransfers = new ArrayList<Bot>();
    private final List<Bot> botCollects = new ArrayList<Bot>();
    private int numActiveOrderBot = 0;

    public void initialize() {
        this.updaterService = new BotUpdaterService(this);
        this.updaterService.setName("BotUpdaterService");
        this.updaterService.start();
    }

    public List<Bot> getBotOrdersByLocation(int mapId, int zoneId) {
        List<Bot> bots = this.botOrdersByLocation.get(LocationIndex.of(mapId, zoneId));
        if (bots == null) {
            bots = new ArrayList<Bot>();
        }
        return bots;
    }

    public void addBotOrder(Bot bot) {
        this.botOrders.add(bot);
        this.indexBotOrderLocation(bot);
    }

    public void indexBotOrderLocation(Bot bot) {
        LocationIndex index = LocationIndex.of(bot.getMapId(), bot.getZoneId());
        List bots = this.botOrdersByLocation.computeIfAbsent(index, k -> new ArrayList());
        bots.add(bot);
    }

    public void unindexBotOrderLocation(Bot bot) {
        LocationIndex index = LocationIndex.of(bot.getMapId(), bot.getZoneId());
        List<Bot> bots = this.botOrdersByLocation.get(index);
        if (bots != null) {
            bots.removeIf(b -> b.getId() == bot.getId());
            if (bots.isEmpty()) {
                this.botOrdersByLocation.remove(index);
            }
        }
    }

    public Bot getBotOrder(int botId) {
        for (Bot bot : this.botOrders) {
            if (bot.getId() != botId) continue;
            return bot;
        }
        return null;
    }

    /**
     * Add a collect bot to the in-memory list.
     * Called by BotUpdaterService.refreshBotCollects() after loading from GET /api/client/bots/collect.
     */
    public void addBotCollect(Bot bot) {
        this.botCollects.add(bot);
    }

    private List<Bot> getBotsByRole(int role) {
        switch (role) {
            case 0: {
                return this.botOrders;
            }
            case 1: {
                return this.botTransfers;
            }
            case 2: {
                return this.botCollects;
            }
        }
        return new ArrayList<Bot>();
    }

    public boolean removeById(int role, int id) {
        List<Bot> bots = this.getBotsByRole(role);
        for (int i = 0; i < bots.size(); ++i) {
            Bot bot = bots.get(i);
            if (bot.getId() != id) continue;
            bot.destroy();
            bots.remove(i);
            return true;
        }
        return false;
    }

    public void clear(int role) {
        List<Bot> bots = this.getBotsByRole(role);
        bots.forEach(Bot::destroy);
        bots.clear();
    }

    public boolean exist(int serverId, String name, int comparing) {
        Predicate<Bot> filter = bot -> bot.getServer().getId() == serverId && bot.getMyChar().getName().equals(name);
        boolean exist = false;
        switch (comparing) {
            case 0: {
                exist = this.botOrders.stream().anyMatch(filter) || this.botTransfers.stream().anyMatch(filter) || this.botCollects.stream().anyMatch(filter);
                break;
            }
            case 1: {
                exist = this.botOrders.stream().anyMatch(filter);
                break;
            }
            case 2: {
                exist = this.botTransfers.stream().anyMatch(filter);
                break;
            }
            case 3: {
                exist = this.botCollects.stream().anyMatch(filter);
                break;
            }
            case 4: {
                exist = this.botTransfers.stream().anyMatch(filter) || this.botCollects.stream().anyMatch(filter);
            }
        }
        return exist;
    }

    public synchronized void plusNumActiveOrderBot(int value) {
        this.numActiveOrderBot += value;
    }

    public List<Bot> getBotOrders() {
        return this.botOrders;
    }

    public Map<LocationIndex, List<Bot>> getBotOrdersByLocation() {
        return this.botOrdersByLocation;
    }

    public List<Bot> getBotTransfers() {
        return this.botTransfers;
    }

    public List<Bot> getBotCollects() {
        return this.botCollects;
    }

    public static BotService getInstance() {
        return instance;
    }

    public BotUpdaterService getUpdaterService() {
        return this.updaterService;
    }

    public int getNumActiveOrderBot() {
        return this.numActiveOrderBot;
    }

    private static class LocationIndex {
        private int mapId;
        private int zoneId;

        private static LocationIndex of(int mapId, int zoneId) {
            return new LocationIndex(mapId, zoneId);
        }

        public boolean equals(Object obj) {
            if (obj instanceof LocationIndex) {
                LocationIndex comparisonObj = (LocationIndex)obj;
                return this.mapId == comparisonObj.mapId && this.zoneId == comparisonObj.zoneId;
            }
            return false;
        }

        public int hashCode() {
            return this.mapId * 31 + this.zoneId;
        }

        public LocationIndex(int mapId, int zoneId) {
            this.mapId = mapId;
            this.zoneId = zoneId;
        }
    }
}

