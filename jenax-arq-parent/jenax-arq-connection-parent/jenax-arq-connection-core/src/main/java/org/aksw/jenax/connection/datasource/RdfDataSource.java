package org.aksw.jenax.connection.datasource;

import java.util.function.Function;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.datasource.RdfDataEngines;
import org.aksw.jenax.connection.dataengine.RdfDataEngine;
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

    /** Convenience method for applying decorators */
    default <O extends RdfDataSource> O decorate(Function<? super RdfDataSource, O> decorator) {
        return decorator.apply(this);
    }

    /**
     * Return a connection-less QueryExecutionFactory view of this data source.
     * Every QueryExecution created with the returned factory will obtain a fresh
     * connection using {@link #getConnection()} upon execution.
     * The connection is closed when closing the {@link QueryExecution}
     * (NOT the {@link org.apache.jena.query.QueryExecutionFactory})!
     * Consequently, the use of connection pooling is recommended in cases where calling
     * {@link #getConnection()} is expensive.
     */
    default QueryExecutionFactory asQef() {
        return QueryExecutionFactories.of(this);
    }
}
