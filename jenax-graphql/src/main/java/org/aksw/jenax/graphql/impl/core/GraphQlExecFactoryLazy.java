package org.aksw.jenax.graphql.impl.core;

import java.util.concurrent.Callable;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import graphql.language.Document;

class ComputeOnce<T>
{
    protected Callable<ListenableFuture<T>> computationTask;
    protected transient ListenableFuture<T> computation = null;

    public ComputeOnce(Callable<ListenableFuture<T>> computationTask) {
        super();
        this.computationTask = computationTask;
    }

    public static <T> ComputeOnce<T> of(Callable<ListenableFuture<T>> computationTask) {
        return new ComputeOnce<>(computationTask);
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

//    protected void beforeFutureCreation() { }
//    protected void afterFutureCreation() { }
}

public class GraphQlExecFactoryLazy
    implements GraphQlExecFactory
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryLazy.class);

    protected ComputeOnce<GraphQlExecFactory> delegateCreation;

    public GraphQlExecFactoryLazy(ComputeOnce<GraphQlExecFactory> delegateCreation) {
        super();
        this.delegateCreation = delegateCreation;
    }

    public static GraphQlExecFactoryLazy of(Callable<ListenableFuture<GraphQlExecFactory>> delegateCreation) {
        ComputeOnce<GraphQlExecFactory> once = ComputeOnce.of(() -> {
            if (logger.isInfoEnabled()) {
                logger.info("Submitting up async GraphQlExecFactory creation");
            }
            ListenableFuture<GraphQlExecFactory> r = delegateCreation.call();
            if (logger.isInfoEnabled()) {
                logger.info("Submitted async GraphQlExecFactory creation");
            }
            return r;
        });
        return new GraphQlExecFactoryLazy(once);
    }


    @Override
    public GraphQlExec create(Document document) {
        ListenableFuture<GraphQlExecFactory> future = delegateCreation.get();

        Thread thread = Thread.currentThread();
        boolean hasToWait = !future.isDone();
        if (hasToWait) {
            if (logger.isInfoEnabled()) {
                logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") awaiting GraphQlExecFactory creation");
            }
        }

        GraphQlExecFactory delegate;
        try {
             delegate = Futures.getUnchecked(future);
        } finally {
            if (hasToWait) {
                if (logger.isInfoEnabled()) {
                    logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") unblocked.");
                }
            }
        }
        GraphQlExec result = delegate.create(document);
        return result;
    }
}
