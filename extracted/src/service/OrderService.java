/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import constants.API;
import core.model.Order;
import network.http.Request;
import network.http.Response;
import service.OrderUpdaterService;

public class OrderService {
    public static final String PATH = API.createUrl("/api/client/orders");
    private static final String CREATE_PATH = PATH + "/create";
    private static final String BET_PATH = PATH + "/bet";
    private static final String LOSE_PATH = PATH + "/lose";
    private static final String WIN_PATH = PATH + "/win";
    private static final String REWARD_PATH = PATH + "/reward";
    private static final String ERROR_PATH = PATH + "/error";
    private static final String LOG_PATH = PATH + "/log";
    private static final OrderService instance = new OrderService();
    private OrderUpdaterService updaterService;

    public void initialize() {
        this.updaterService = new OrderUpdaterService(this);
        this.updaterService.setName("OrderUpdaterService");
        this.updaterService.start();
    }

    public Order create(int serverId, String name, String bot, int second, byte type, long timeStart, int coinOrder, int botId, int client) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("serverId", (Number)serverId);
        requestData.addProperty("name", name);
        requestData.addProperty("bot", bot);
        requestData.addProperty("second", (Number)second);
        requestData.addProperty("type", (Number)type);
        requestData.addProperty("timeStart", (Number)timeStart);
        requestData.addProperty("coinOrder", (Number)coinOrder);
        requestData.addProperty("botId", (Number)botId);
        requestData.addProperty("client", (Number)client);
        Request request = new Request(CREATE_PATH, "POST");
        Response response = request.send((JsonElement)requestData);
        if (response != null && response.isSuccess()) {
            JsonObject responseData = response.getData().getAsJsonObject();
            int id = responseData.get("id").getAsInt();
            return new Order(id, serverId, name, bot, second, type, timeStart, coinOrder);
        }
        return null;
    }

    public void bet(int id) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        Request request = new Request(BET_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public void lose(int id, String winName) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        requestData.addProperty("winName", winName);
        Request request = new Request(LOSE_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public void win(int id, int coinWin, int coinFee, int coinReward) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        requestData.addProperty("coinWin", (Number)coinWin);
        requestData.addProperty("coinFee", (Number)coinFee);
        requestData.addProperty("coinReward", (Number)coinReward);
        Request request = new Request(WIN_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public void reward(int id) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        Request request = new Request(REWARD_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public void error(int id, String reason) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        requestData.addProperty("reason", reason);
        Request request = new Request(ERROR_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public void log(int id, String log) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("id", (Number)id);
        requestData.addProperty("log", log);
        Request request = new Request(LOG_PATH, "PUT");
        request.send((JsonElement)requestData);
    }

    public static OrderService getInstance() {
        return instance;
    }

    public OrderUpdaterService getUpdaterService() {
        return this.updaterService;
    }
}

