package org.aksw.jenax.dataaccess.sparql.factory.dataset.connection;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.execution.update.UpdateEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.engine.query.QueryEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.engine.update.UpdateEngineFactoryCore;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.exec.QueryExecutionCompat;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;

/**
 * Similar to {@link RDFConnection#connect(Dataset)} but
 * with additional configuration options to set a {@link Context} and
 * the update / query engines.
 *
 * The result of the builder is of type {@link DatasetRDFConnectionFactory}
 * which is a function from {@link Dataset} to {@link RDFConnection}.
 *
 * This builder is motivated by the quad-engine module
 * which requires configuration of its own query/update engine.
 *
 * @author Claus Stadler
 *
 */
public class DatasetRDFConnectionFactoryBuilder {
    protected QueryEngineFactoryProvider queryEngineFactoryProvider = null;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider = null;

    /** Use {@link ContextAccumulator} ? */
    protected Context context;

    /**
     * This approach is a workaround for a bug with {@link RDFConnection#connect(Dataset)} for at least jena 4.3.x:
     * Due to the bug an internal {@link QueryExecutionCompat} is created whose .get() method returns a null
     * QueryExec instance. The workaround works because the resulting connection uses a different kind of
     * wrapping.
     */
    public static RDFConnection connect(Dataset dataset) {
        return createWithDefaults().build().connect(dataset);
    }

    public static DatasetRDFConnectionFactoryBuilder create() {
        return new DatasetRDFConnectionFactoryBuilder();
    }

    public static DatasetRDFConnectionFactoryBuilder createWithDefaults() {
        return create()
                .setDefaultQueryEngineFactoryProvider()
                .setDefaultUpdateEngineFactoryProvider();
    }

    public DatasetRDFConnectionFactoryBuilder setQueryEngineFactoryProvider(QueryEngineFactoryProvider queryEngineFactoryProvider) {
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        return this;
    }

    /** Set the provider to always provide the given query engine factory */
    public DatasetRDFConnectionFactoryBuilder setQueryEngineFactoryProvider(QueryEngineFactory queryEngineFactory) {
        this.queryEngineFactoryProvider = (query, dataset, context) -> queryEngineFactory;
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setDefaultQueryEngineFactoryProvider() {
        this.queryEngineFactoryProvider = QueryEngineRegistry::findFactory;
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setUpdateEngineFactoryCore(UpdateEngineFactoryCore updateEngineFactoryCore) {
        return setUpdateEngineFactory(updateEngineFactoryCore.asFactory());
    }

    public DatasetRDFConnectionFactoryBuilder setUpdateEngineFactoryProvider(UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
        return this;
    }

    /** Set the provider to always provide the given query engine factory */
    public DatasetRDFConnectionFactoryBuilder setUpdateEngineFactory(UpdateEngineFactory updateEngineFactory) {
        this.updateEngineFactoryProvider = (dataset, context) -> updateEngineFactory;
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setDefaultUpdateEngineFactoryProvider() {
        this.updateEngineFactoryProvider = (dataset, context) -> UpdateEngineRegistry.findFactory(dataset, context);
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public DatasetRDFConnectionFactory build() {
        // The purpose of using this builder is to setup the providers
        // If they are null we assume a mistake
        Objects.requireNonNull(queryEngineFactoryProvider);
        Objects.requireNonNull(updateEngineFactoryProvider);
        return new DatasetRDFConnectionFactoryImpl(context, queryEngineFactoryProvider, updateEngineFactoryProvider);
    }
}
