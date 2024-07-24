package org.aksw.jenax.graphql.legacy;

import java.util.Map;

import org.aksw.jenax.graphql.impl.common.ComputeOnce;
import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactoryDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import graphql.language.Document;
import graphql.language.Value;

@Deprecated // Lazy init was moved to the resolver so that fully qualified queries do not need it
public class GraphQlExecFactoryLazy
    implements GraphQlExecFactoryDocument
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryLazy.class);

    protected ComputeOnce<GraphQlExecFactory> delegateCreation;

    public GraphQlExecFactoryLazy(ComputeOnce<GraphQlExecFactory> delegateCreation) {
        super();
        this.delegateCreation = delegateCreation;
    }

    @Override
    public GraphQlExec create(Document document, Map<String, Value<?>> assignments) {
        ListenableFuture<GraphQlExecFactory> future = delegateCreation.get();

        Thread thread = Thread.currentThread();
        boolean hasToWait = !future.isDone();
        if (hasToWait) {
            if (logger.isInfoEnabled()) {
                logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") awaiting async GraphQlExecFactory creation");
            }
        }

        GraphQlExecFactory delegate;
        try {
             delegate = Futures.getUnchecked(future);
        } finally {
            if (hasToWait) {
                if (logger.isInfoEnabled()) {
                    logger.info("Thread " + thread.getName() + " (id=" + thread.getId() + ") successfully received GraphQlExecfactory");
                }
            }
        }
        GraphQlExec result = delegate.create(document, assignments);
        return result;
    }
}
