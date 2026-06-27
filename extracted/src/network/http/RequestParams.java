/*
 * Decompiled with CFR 0.152.
 */
package network.http;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParams {
    private final Map<String, String> params = new HashMap<String, String>();

    public void add(String key, Object value) {
        this.params.put(key, String.valueOf(value));
    }

    public String get() {
        String queryString = this.params.entrySet().stream().map(p -> (String)p.getKey() + "=" + this.customEncode((String)p.getValue())).collect(Collectors.joining("&"));
        return queryString.isEmpty() ? "" : "?" + queryString;
    }

    private String customEncode(String value) {
        String encodedValue = this.encode(value);
        encodedValue = encodedValue.replace("[", "%5B").replace("]", "%5D");
        return encodedValue;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (Exception e) {
            return value;
        }
    }

    public String toString() {
        return this.get();
    }
}

