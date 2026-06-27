/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package network.http;

import com.google.gson.JsonElement;

public class Response {
    private int status;
    private JsonElement data;

    public boolean isSuccess() {
        return this.status == 200;
    }

    public Response(int status, JsonElement data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return this.status;
    }

    public JsonElement getData() {
        return this.data;
    }
}

