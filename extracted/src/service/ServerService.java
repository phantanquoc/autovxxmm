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
import constants.API;
import core.model.Server;
import java.util.ArrayList;
import java.util.List;
import network.http.Request;
import network.http.Response;

public class ServerService {
    public static final String PATH = API.createUrl("/api/resource/server");
    private static final ServerService instance = new ServerService();
    private final List<Server> servers = new ArrayList<Server>();

    public void loadServers() {
        Request request = new Request(PATH, "GET");
        Response response = request.send();
        if (response != null && response.isSuccess()) {
            JsonArray serversData = response.getData().getAsJsonArray();
            for (int i = 0; i < serversData.size(); ++i) {
                JsonObject serverData = serversData.get(i).getAsJsonObject();
                Server server = new Server();
                server.setId(serverData.get("id").getAsInt());
                server.setName(serverData.get("name").getAsString());
                server.setIp(serverData.get("ip").getAsString());
                server.setPort(serverData.get("port").getAsInt());
                server.setType(serverData.get("type").getAsByte());
                this.servers.add(server);
            }
        }
    }

    public Server getServer(int serverId) {
        return this.servers.stream().filter(s -> s.getId() == serverId).findFirst().orElse(null);
    }

    public String getServerName(int serverId) {
        Server server = this.getServer(serverId);
        return server == null ? "" : server.getName();
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public static ServerService getInstance() {
        return instance;
    }
}

