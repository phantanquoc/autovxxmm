/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import utils.Res;

public class HeartBeat
implements Runnable {
    public static final int ICON = 1;
    private static final long CHECK_INTERVAL = 60000L;
    private Bot bot;
    public boolean onThread;
    private long lastTimeCheck;
    private final Object lock = new Object();

    public HeartBeat(Bot bot) {
        this.bot = bot;
    }

    @Override
    public final void run() {
        while (this.onThread) {
            Res.sleep(1000L);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void alive() {
        Object object = this.lock;
        synchronized (object) {
            this.lock.notifyAll();
        }
    }
}

