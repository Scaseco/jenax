package org.aksw.jenax.web.util;

import jakarta.ws.rs.container.AsyncResponse;

/**
 * A simple wrapper for a runnable that cancels the response should the
 * provided delegate fail
 *
 * @author raven
 *
 */
public class RunnableAsyncResponseSafe
    implements Runnable {

    private AsyncResponse asyncResponse;
    private Runnable delegate;

    public RunnableAsyncResponseSafe(AsyncResponse asyncResponse, Runnable delegate) {
        this.asyncResponse = asyncResponse;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try {
            delegate.run();
        } catch(Exception e) {
            asyncResponse.cancel();
            throw new RuntimeException(e);
        }
    }
}
