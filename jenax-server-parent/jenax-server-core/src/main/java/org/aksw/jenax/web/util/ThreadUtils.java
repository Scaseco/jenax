package org.aksw.jenax.web.util;

import jakarta.ws.rs.container.AsyncResponse;

public class ThreadUtils {

    public static void start(AsyncResponse asyncResponse, Runnable runnable) {
        Runnable safeRunnable = new RunnableAsyncResponseSafe(asyncResponse, runnable);

        new Thread(safeRunnable).start();
    }
}
