package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecWrapperBase<T extends QueryExec>
    implements QueryExecWrapper
{
    protected T delegate;

    public QueryExecWrapperBase(T decoratee) {
        super();
        this.delegate = decoratee;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}
