/*
 * Decompiled with CFR 0.152.
 */
package utils;

import java.io.InputStream;
import main.Application;

public class Resources {
    public static byte[] read(String path) {
        try {
            InputStream is = Application.class.getResource(path).openStream();
            byte[] bs = new byte[is.available()];
            is.read(bs);
            is.close();
            return bs;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

