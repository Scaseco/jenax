package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import org.apache.jena.sparql.exec.QueryExecBuilder;

public class QueryExecBuilderWrapperBase
    implements QueryExecBuilderWrapper<QueryExecBuilder>
{
    protected QueryExecBuilder delegate;

    public QueryExecBuilderWrapperBase(QueryExecBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public QueryExecBuilder getDelegate() {
        return delegate;
    }
}
