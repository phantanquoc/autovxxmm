/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.http.Header
 *  org.apache.http.message.BasicHeader
 */
package network.http;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class RequestHeaders {
    private final Map<String, String> headers = new HashMap<String, String>();

    public void add(String name, String value) {
        this.headers.put(name, value);
    }

    public Header[] get() {
        Header[] headers = new Header[this.headers.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            headers[i] = new BasicHeader(entry.getKey(), entry.getValue());
            ++i;
        }
        return headers;
    }
}

