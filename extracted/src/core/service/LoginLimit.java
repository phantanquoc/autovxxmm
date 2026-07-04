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
    private static long lastLoginTime = 0L;
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
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return LoginLimit.acquireGlobalSlot();
    }

    // Van nhịp toàn cục: chỉ cho 1 login đi qua mỗi loginInterval giây trên TOÀN tool
    // (mọi server dùng chung cùng 1 IP) => chống burst kết nối khiến server ban IP.
    // Trả false nếu chưa tới nhịp -> bot tự reconnect chờ tiếp, KHÔNG mở socket.
    private static synchronized boolean acquireGlobalSlot() {
        long interval = (long)SettingService.getInstance().getLoginInterval() * 1000L;
        long now = Res.t();
        if (interval > 0L && now - lastLoginTime < interval) {
            return false;
        }
        lastLoginTime = now;
        return true;
    }
}

