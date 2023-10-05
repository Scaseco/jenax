package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:54 AM
 */
public interface QueryExecutionFactory
    extends QueryExecutionFactoryString, QueryExecutionFactoryQuery, AutoCloseable
{
    String getId();
    String getState();

    <T> T unwrap(Class<T> clazz);

//    void close();
}
