package org.aksw.jenax.graphql.impl.common;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class ComputeOnce<T>
{
    private static final Logger logger = LoggerFactory.getLogger(ComputeOnce.class);

    protected String taskName;
    protected Callable<ListenableFuture<T>> computationTask;
    protected transient ListenableFuture<T> computation = null;

    public ComputeOnce(String taskName, Callable<ListenableFuture<T>> computationTask) {
        super();
        this.taskName = taskName;
        this.computationTask = computationTask;
    }

    public static <T> ComputeOnce<T> of(String taskName, Callable<ListenableFuture<T>> computationTask) {
        return new ComputeOnce<>(taskName, computationTask);
    }

    public static <T> ComputeOnce<T> ofWithLogging(String taskName, Callable<ListenableFuture<T>> delegateCreation) {
        ComputeOnce<T> result = new ComputeOnce<>(taskName, () -> {
            if (logger.isInfoEnabled()) {
                logger.info("Submitting task for async " + taskName + " creation");
            }
            ListenableFuture<T> r = delegateCreation.call();
            if (logger.isInfoEnabled()) {
                logger.info("Successfully submitted task for async " + taskName + " creation");
            }
            return r;
        });
        return result;
    }


    public ListenableFuture<T> get() {
        if (computation == null) {
            synchronized (this) {
                if (computation == null) {
                    try {
                        // beforeFutureCreation();
                        computation = computationTask.call();
                        // afterFutureCreation();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return computation;
    }

    public T getWithLogging() {
        return getWithLogging(this);
    }

    public static <T> T getWithLogging(ComputeOnce<T> delegateCreation) {
        ListenableFuture<T> future = delegateCreation.get();

        Thread thread = Thread.currentThread();
        boolean hasToWait = !future.isDone();
        if (hasToWait) {
            if (logger.isInfoEnabled()) {
                logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") awaiting async GraphQlExecFactory creation");
            }
        }

        T delegate;
        try {
             delegate = Futures.getUnchecked(future);
        } finally {
            if (hasToWait) {
                if (logger.isInfoEnabled()) {
                    logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") successfully received GraphQlExecfactory");
                }
            }
        }
        return delegate;
    }
}
