package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceAdapter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecutionBuilderAdapter;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecutionBuilderAdapter;
import org.apache.jena.update.UpdateExecutionBuilder;

/**
 * A factory/supplier of RDFConnection instances. Similar to a JDBC DataSource.
 *
 * This interface does not provide resource management, i.e. a close() method.
 * It should only be used as follows:
 * (a) As a lambda in conjunction with {@link RdfDataEngines#of(RDFDataSource, AutoCloseable)}
 * (b) in consuming code that does not need resource management
 *
 * Prefer {@link RDFEngine} whenever resources may need to be closed.
 *
 */
@FunctionalInterface
public interface RDFDataSource
{
    RDFConnection getConnection();

    default Dataset getDataset() {
        return null;
    }

//    default RDFConnection getConnection() {
//        RDFLinkSource linkSource = asLinkSource();
//        RDFLink link = linkSource.newLink();
//        return RDFConnectionAdapter.adapt(link);
//    }

    default RDFLinkSource asLinkSource() {
        return RDFLinkSourceAdapter.adapt(this);
    }

    /**
     * Obtain a QueryExecutionBuilder that will execute the built query on a fresh connection.
     * The connection will be acquired only before execution and will be
     * immediately closed afterwards.
     *
     * @since 5.3.0-1
     */
    default QueryExecutionBuilder newQuery() {
        RDFLinkSource linkSource = asLinkSource();
        QueryExecBuilder execBuilder = linkSource.newQuery();
        return QueryExecutionBuilderAdapter.adapt(execBuilder);
    }

    default UpdateExecutionBuilder newUpdate() {
        RDFLinkSource linkSource = asLinkSource();
        UpdateExecBuilder execBuilder = linkSource.newUpdate();
        return UpdateExecutionBuilderAdapter.adapt(execBuilder);
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
    @Deprecated
    default QueryExecutionFactory asQef() {
        return QueryExecutionFactories.of(this);
    }
}
