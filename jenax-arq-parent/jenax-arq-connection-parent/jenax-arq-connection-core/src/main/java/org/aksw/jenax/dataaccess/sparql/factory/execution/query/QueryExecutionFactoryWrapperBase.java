package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryWrapperBase<T extends QueryExecutionFactory>
    implements QueryExecutionFactory
{
    protected T decoratee;

    public QueryExecutionFactoryWrapperBase(T decoratee) {
        super();
        this.decoratee = decoratee;
    }

    public T getDelegate() {
        return decoratee;
    }

    @Override
    public String getId() {
        return getDelegate().getId();
    }

    @Override
    public String getState() {
        return getDelegate().getState();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return getDelegate().createQueryExecution(query);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return getDelegate().createQueryExecution(queryString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result = getClass().isAssignableFrom(clazz)
			? (T)this
			: decoratee.unwrap(clazz);

        return result;
    }

    @Override
    public void close() throws Exception {
        decoratee.close();
    }
}
