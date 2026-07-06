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
import core.model.TradeLog;
import network.http.Request;
import service.AsyncExecutor;

public class TradeLogService {
    private static final String PATH = API.createUrl("/api/log/trade");

    public static void save(TradeLog tradeLog) {
        AsyncExecutor.submit(() -> {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("serverId", (Number)tradeLog.getServerId());
            requestData.addProperty("name", tradeLog.getName());
            requestData.addProperty("customer", tradeLog.getCustomer());
            requestData.addProperty("before", (Number)tradeLog.getBefore());
            requestData.addProperty("after", (Number)tradeLog.getAfter());
            requestData.addProperty("change", (Number)tradeLog.getChange());
            requestData.addProperty("description", tradeLog.getDescription());
            requestData.addProperty("type", (Number)tradeLog.getType());
            requestData.addProperty("timeStart", (Number)tradeLog.getTimeStart());
            requestData.addProperty("timeStop", (Number)tradeLog.getTimeStop());
            Request request = new Request(PATH, "PUT");
            request.send((JsonElement)requestData);
        });
    }
}

