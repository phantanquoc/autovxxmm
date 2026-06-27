/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import service.BotService;
import service.SettingService;
import utils.Res;

public class LoginLimit {
    private static final Map<Integer, LoginLimit> instances = new HashMap<Integer, LoginLimit>();
    private final int serverId;

    private LoginLimit(int serverId) {
        this.serverId = serverId;
    }

    public static synchronized LoginLimit getInstance(int serverId) {
        return instances.computeIfAbsent(serverId, LoginLimit::new);
    }

    public synchronized boolean checkLimit(Bot bot) {
        try {
            List<Bot> bots = BotService.getInstance().getBotOrders();
            int count = 0;
            for (int i = 0; i < bots.size(); ++i) {
                Bot botChecking = bots.get(i);
                if (!botChecking.isEnable() || botChecking.getServer().getId() != this.serverId || botChecking.isOnline() || bot.getId() == botChecking.getId() || !botChecking.getAutoLogin().isLoginSubmiting || ++count < SettingService.getInstance().getLoginLimit()) continue;
                return false;
            }
            Res.sleep(250L);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

