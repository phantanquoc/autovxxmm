/*
 * Shared executor pool for fire-and-forget HTTP calls (order updates, trade logs).
 * Daemon threads so the pool does not prevent JVM shutdown.
 */
package service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import utils.Res;

public class AsyncExecutor {
    private static final int CORE_THREADS = 2;
    private static final int MAX_THREADS = 4;
    private static final long KEEP_ALIVE_SECONDS = 60L;

    private static final RejectedExecutionHandler DROP_WITH_LOG = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor ex) {
            System.err.println(Res.cTime() + " AsyncExecutor: hang doi day, bo 1 task async ghi so backend");
        }
    };

    private static final ExecutorService executor = new ThreadPoolExecutor(
            CORE_THREADS,
            MAX_THREADS,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(256),
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "AsyncExecutor-" + (++count));
                    t.setDaemon(true);
                    return t;
                }
            },
            DROP_WITH_LOG
    );

    public static void submit(Runnable task) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }
}
