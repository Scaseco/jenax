package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * A factory/supplier of RDFConnection instances. Similar to a JDBC DataSource.
 *
 * This interface does not provide resource management, i.e. a close() method.
 * It should only be used as follows:
 * (a) As a lambda in conjunction with {@link RdfDataEngines#of(RdfDataSource, AutoCloseable)}
 * (b) in consuming code that does not need resource management
 *
 * Prefer {@link RdfDataEngine} whenever resources may need to be closed.
 *
 */
@FunctionalInterface
public interface RdfDataSource
{
    RDFConnection getConnection();

    /** Convenience method for applying decorators. Returns a new RdfDatasource that wraps this one. */
    default RdfDataSource decorate(RdfDataSourceTransform rdfDataSourceTransform) {
        return rdfDataSourceTransform.apply(this);
    }

    /**
     * Return a connection-less QueryExecutionFactory view of this data source.
     * Every QueryExecution created with the returned factory will obtain a fresh
     * connection using {@link #getConnection()} upon execution.
     * The connection is closed when closing the {@link QueryExecution}.
     * Consequently, the use of connection pooling is recommended in cases where calling
     * {@link #getConnection()} is expensive.
     * The behavior of {@link QueryExecutionFactory#close()} is implementation dependent.
     * By default it is a no-op but it may close the data source.
     */
    default QueryExecutionFactory asQef() {
        return QueryExecutionFactories.of(this);
    }
}
