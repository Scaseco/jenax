package org.aksw.jenax.graphql.impl.sparql;

import java.util.concurrent.Callable;

import org.aksw.jenax.graphql.impl.core.ComputeOnce;

import com.google.common.util.concurrent.ListenableFuture;

public class GraphQlResolverImplLazy
    implements GraphQlResolverWrapper
{
    protected ComputeOnce<GraphQlResolver> delegateCreation;

    public GraphQlResolverImplLazy(ComputeOnce<GraphQlResolver> delegateCreation) {
        super();
        this.delegateCreation = delegateCreation;
    }

    public static GraphQlResolver of(Callable<ListenableFuture<GraphQlResolver>> delegateCreation) {
        return new GraphQlResolverImplLazy(ComputeOnce.ofWithLogging("GraphQlResolver", delegateCreation));
    }

    @Override
    public GraphQlResolver getDelegate() {
        return delegateCreation.getWithLogging();
    }
}
