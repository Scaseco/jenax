package org.aksw.jena_sparql_api.core;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:39 PM
 */
public abstract class QueryExecutionFactoryBase
    implements QueryExecutionFactory
{
    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
    }

    @Override
    public void close() {
        // Noop by default
    }
}
