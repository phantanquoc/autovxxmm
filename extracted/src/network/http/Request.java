/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.apache.http.HttpEntity
 *  org.apache.http.HttpResponse
 *  org.apache.http.client.ServiceUnavailableRetryStrategy
 *  org.apache.http.client.config.RequestConfig
 *  org.apache.http.client.methods.CloseableHttpResponse
 *  org.apache.http.client.methods.HttpGet
 *  org.apache.http.client.methods.HttpPost
 *  org.apache.http.client.methods.HttpPut
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.conn.HttpHostConnectException
 *  org.apache.http.entity.StringEntity
 *  org.apache.http.impl.client.CloseableHttpClient
 *  org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy
 *  org.apache.http.impl.client.HttpClients
 *  org.apache.http.util.EntityUtils
 */
package network.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import constants.API;
import java.nio.charset.StandardCharsets;
import main.Application;
import network.http.RequestHeaders;
import network.http.RequestParams;
import network.http.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import utils.Res;

public class Request {
    private static final CloseableHttpClient httpClient;
    private static final String LOGIN_PATH;
    private static final String HEALTH_PATH;
    private static final int SOCKET_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_INTERVAL = 500;
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    private static String username;
    private static String password;
    private static String authenticationToken;
    private final String url;
    private final String method;
    private final RequestParams params;
    private final RequestHeaders headers;

    public Request(String url, String method) {
        this.url = url;
        this.method = method;
        this.headers = new RequestHeaders();
        this.params = new RequestParams();
    }

    public Response send() {
        return this.send(null);
    }

    public Response send(JsonElement requestData) {
        int attemp = 0;
        while (true) {
            try {
                while (true) {
                    HttpUriRequest request;
                    CloseableHttpResponse httpResponse;
                    int statusCode;
                    if ((statusCode = (httpResponse = httpClient.execute(request = this.createRequest(requestData))).getStatusLine().getStatusCode()) == 200) {
                        JsonElement responseData = this.getResponseData((HttpResponse)httpResponse);
                        return new Response(statusCode, responseData);
                    }
                    if (statusCode == 401) {
                        if (authenticationToken == null) {
                            JsonElement responseData = this.getResponseData((HttpResponse)httpResponse);
                            return new Response(statusCode, responseData);
                        }
                        Application.alert("Phi\u00ean \u0111\u0103ng nh\u1eadp \u0111\u00e3 h\u1ebft h\u1ea1n!");
                        System.exit(0);
                        continue;
                    }
                    if (++attemp >= 10) {
                        return new Response(statusCode, null);
                    }
                    Res.sleep(500L);
                }
            }
            catch (HttpHostConnectException request) {
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    private HttpUriRequest createRequest(JsonElement requestData) {
        String url = this.url + this.params.get();
        this.headers.add("Client-Type", "java");
        this.headers.add("Content-Type", "application/json; charset=utf-8");
        if (authenticationToken != null) {
            this.headers.add("Authorization", "Bearer " + authenticationToken);
        }
        HttpUriRequest request = this.createRequest(this.method, url, requestData);
        request.setHeaders(this.headers.get());
        return request;
    }

    private HttpUriRequest createRequest(String method, String url, JsonElement requestData) {
        StringEntity entity = null;
        if (requestData != null) {
            entity = new StringEntity(requestData.toString(), StandardCharsets.UTF_8);
        }
        switch (method) {
            case "GET": {
                return new HttpGet(url);
            }
            case "POST": {
                HttpPost postRequest = new HttpPost(url);
                if (entity != null) {
                    postRequest.setEntity((HttpEntity)entity);
                }
                return postRequest;
            }
            case "PUT": {
                HttpPut putRequest = new HttpPut(url);
                if (entity != null) {
                    putRequest.setEntity((HttpEntity)entity);
                }
                return putRequest;
            }
        }
        throw new IllegalArgumentException("Invalid HTTP method");
    }

    private JsonElement getResponseData(HttpResponse response) {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String data = EntityUtils.toString((HttpEntity)entity);
                return JsonParser.parseString((String)data);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean authenticate() {
        Request request = new Request(LOGIN_PATH, POST);
        JsonObject requestData = new JsonObject();
        requestData.addProperty("username", username);
        requestData.addProperty("password", password);
        Response response = request.send((JsonElement)requestData);
        if (response.getStatus() == 200) {
            JsonObject responseData = response.getData().getAsJsonObject();
            authenticationToken = responseData.get("jwt").getAsString();
            return true;
        }
        if (response.getStatus() == 401) {
            JsonObject responseData = response.getData().getAsJsonObject();
            String message = responseData.get("message").getAsString();
            Application.warning(message);
            return false;
        }
        return false;
    }

    public static void setUsername(String username) {
        Request.username = username;
    }

    public static void setPassword(String password) {
        Request.password = password;
    }

    public RequestParams getParams() {
        return this.params;
    }

    public RequestHeaders getHeaders() {
        return this.headers;
    }

    static {
        LOGIN_PATH = API.createUrl("/api/login");
        HEALTH_PATH = API.createUrl("/api/health");
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).build();
        DefaultServiceUnavailableRetryStrategy retryStrategy = new DefaultServiceUnavailableRetryStrategy(10, 500);
        httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setServiceUnavailableRetryStrategy((ServiceUnavailableRetryStrategy)retryStrategy).build();
    }
}

