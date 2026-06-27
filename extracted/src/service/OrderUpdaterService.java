/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 */
package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.model.Bot;
import core.model.Order;
import core.module.impl.OrderScreen;
import java.util.ArrayList;
import java.util.List;
import main.Application;
import network.http.Request;
import network.http.Response;
import service.BotService;
import service.OrderService;
import utils.Res;

public class OrderUpdaterService
extends Thread {
    private static final long CHECK_UPDATE_INTERVAL = 10000L;
    private final OrderService service;
    private boolean getOrdersQueue = false;
    private long timeCheckUpdate;

    public OrderUpdaterService(OrderService service) {
        this.service = service;
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    Res.sleep(1000L);
                    if (!BotService.getInstance().getUpdaterService().isGetBots()) continue;
                    this.update();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    private void update() throws Exception {
        if (Res.t() >= this.timeCheckUpdate) {
            this.checkUpdate();
            this.timeCheckUpdate = Res.t() + 10000L;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkUpdate() {
        Request request = this.createRequest(OrderService.PATH, "/check-update", "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonObject responseData = response.getData().getAsJsonObject();
            JsonArray ordersUpdateStatusData = responseData.getAsJsonArray("ordersUpdateStatus");
            ArrayList ordersUpdateStatus = new ArrayList();
            ordersUpdateStatusData.forEach(orderData -> {
                JsonObject order = orderData.getAsJsonObject();
                int id = order.get("id").getAsInt();
                int botId = order.get("botId").getAsInt();
                int serverId = order.get("serverId").getAsInt();
                int status = order.get("status").getAsInt();
                ordersUpdateStatus.add(new OrderUpdateStatus(id, botId, serverId, status));
            });
            if (!ordersUpdateStatus.isEmpty()) {
                List<Bot> botOrders;
                List<Bot> list = botOrders = BotService.getInstance().getBotOrders();
                synchronized (list) {
                    for (Bot bot : botOrders) {
                        for (int i = 0; i < ordersUpdateStatus.size(); ++i) {
                            OrderUpdateStatus orderUpdateStatus = (OrderUpdateStatus)ordersUpdateStatus.get(i);
                            if (bot.getId() != orderUpdateStatus.getBotId() || bot.getServer().getId() != orderUpdateStatus.getServerId()) continue;
                            OrderScreen screen = bot.getScreen().orderScreen();
                            Order order = screen.getOrder();
                            if (order != null && order.getId() == orderUpdateStatus.getId()) {
                                order.setStatus(orderUpdateStatus.getStatus());
                                if (order.hasStatus(2) || order.hasStatus(4) || order.hasStatus(5)) {
                                    screen.setOrder(null);
                                }
                            }
                            ordersUpdateStatus.remove(i);
                            --i;
                        }
                    }
                }
            }
            JsonArray ordersDeleteData = responseData.getAsJsonArray("ordersDelete");
            ArrayList ordersDelete = new ArrayList();
            ordersDeleteData.forEach(orderData -> {
                JsonObject orderDeleteData = orderData.getAsJsonObject();
                int id = orderDeleteData.get("id").getAsInt();
                int botId = orderDeleteData.get("botId").getAsInt();
                int serverId = orderDeleteData.get("serverId").getAsInt();
                ordersDelete.add(new OrderDelete(id, botId, serverId));
            });
            if (!ordersDelete.isEmpty()) {
                List<Bot> botOrders;
                List<Bot> list = botOrders = BotService.getInstance().getBotOrders();
                synchronized (list) {
                    for (Bot bot : botOrders) {
                        for (int i = 0; i < ordersDelete.size(); ++i) {
                            OrderDelete orderDelete = (OrderDelete)ordersDelete.get(i);
                            if (bot.getId() != orderDelete.getBotId() || bot.getServer().getId() != orderDelete.getServerId()) continue;
                            OrderScreen screen = bot.getScreen().orderScreen();
                            Order order = screen.getOrder();
                            if (order != null && order.getId() == orderDelete.getId()) {
                                screen.setOrder(null);
                            }
                            ordersDelete.remove(i);
                            --i;
                        }
                    }
                }
            }
        }
    }

    private Request createRequest(String path, String uri, String method) {
        StringBuilder pathBuilder = new StringBuilder(path);
        if (uri != null) {
            pathBuilder.append(uri);
        }
        Request request = new Request(pathBuilder.toString(), method);
        if (Application.isSplitClient) {
            request.getHeaders().add("client", String.valueOf(Application.client));
        }
        return request;
    }

    private static class OrderDelete {
        private int id;
        private int botId;
        private int ServerId;

        public OrderDelete(int id, int botId, int ServerId) {
            this.id = id;
            this.botId = botId;
            this.ServerId = ServerId;
        }

        public int getId() {
            return this.id;
        }

        public int getBotId() {
            return this.botId;
        }

        public int getServerId() {
            return this.ServerId;
        }
    }

    private static class OrderUpdateStatus {
        private int id;
        private int botId;
        private int serverId;
        private int status;

        public OrderUpdateStatus(int id, int botId, int serverId, int status) {
            this.id = id;
            this.botId = botId;
            this.serverId = serverId;
            this.status = status;
        }

        public int getId() {
            return this.id;
        }

        public int getBotId() {
            return this.botId;
        }

        public int getServerId() {
            return this.serverId;
        }

        public int getStatus() {
            return this.status;
        }
    }
}

