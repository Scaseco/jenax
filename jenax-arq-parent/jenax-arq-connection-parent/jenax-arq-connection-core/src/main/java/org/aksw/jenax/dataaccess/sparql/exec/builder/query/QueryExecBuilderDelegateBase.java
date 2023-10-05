package org.aksw.jenax.dataaccess.sparql.exec.builder.query;

import org.apache.jena.sparql.exec.QueryExecBuilder;

public class QueryExecBuilderDelegateBase
    implements QueryExecBuilderDelegate
{
    protected QueryExecBuilder delegate;

    public QueryExecBuilderDelegateBase(QueryExecBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public QueryExecBuilder getDelegate() {
        return delegate;
    }
}
