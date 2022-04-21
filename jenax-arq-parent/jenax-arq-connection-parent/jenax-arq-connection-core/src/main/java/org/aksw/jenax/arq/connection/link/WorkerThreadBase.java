package org.aksw.jenax.arq.connection.link;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.rdflink.RDFLink;

/**
 * Wraps an {@link RDFLink} such that all method calls originate from the same
 * (worker) thread.
 *
 * Transactions in jena are per-thread rather than per-connection. This means,
 * that if a main thread starts a write transaction on a connection and later a worker
 * thread wants to perform a write on the same connection it results in a deadlock.
 *
 * This wrapper resolves the deadlock situation at the cost of processing any request
 * from any thread made to the RDFLink sequentially in a single additional thread.
 *
 * Note that this class also wraps access to QueryExec, RowSet, etc such that it goes through the worker thread
 *
 *
 * @author raven
 *
 */

public class WorkerThreadBase {
    protected ExecutorService es;


    public WorkerThreadBase() {
        this(null);
    }

    public WorkerThreadBase(ExecutorService es) {
        super();
        this.es = es;
    }

    public static <T> T submit(ExecutorService executorService, Callable<T> callable) {
        try {
            return executorService.submit(callable).get();
        // } catch (InterruptedException | ExecutionException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void submit(Runnable runnable) {
        submit(() -> { runnable.run(); return null; });
    }

    public <T> T submit(Callable<T> callable) {
        if (es == null) {
            synchronized (this) {
                if (es == null) {
                    es = Executors.newSingleThreadExecutor();
                }
            }
        }

        T result = submit(es, callable);
        return result;
    }

}