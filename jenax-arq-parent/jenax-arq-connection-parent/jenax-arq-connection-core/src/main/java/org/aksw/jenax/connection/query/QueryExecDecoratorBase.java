package org.aksw.jenax.connection.query;

import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecDecoratorBase<T extends QueryExec>
    implements QueryExecDecorator
{
    protected T decoratee;

    public QueryExecDecoratorBase(T decoratee) {
        super();
        this.decoratee = decoratee;
    }

    @Override
    public T getDecoratee() {
        return decoratee;
    }
}
