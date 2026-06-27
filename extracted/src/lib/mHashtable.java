/*
 * Decompiled with CFR 0.152.
 */
package lib;

import java.util.Enumeration;
import java.util.Hashtable;

public class mHashtable {
    public Hashtable h = new Hashtable();

    public Object get(Object k) {
        return this.h.get(k);
    }

    public Object conskey(String key) {
        return this.h.get(key);
    }

    public void clear() {
        this.h.clear();
    }

    public Enumeration keys() {
        return this.h.keys();
    }

    public int size() {
        return this.h.size();
    }

    public void put(Object k, Object v) {
        if (this.h.containsKey(k)) {
            this.h.remove(k);
        }
        this.h.put(k, v);
    }

    public void remove(Object k) {
        this.h.remove(k);
    }

    public void Remove(String key) {
        this.h.remove(key);
    }
}

