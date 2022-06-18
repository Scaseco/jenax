package org.aksw.jenax.arq.connection.dataset;

import java.util.function.Supplier;

import org.aksw.jenax.arq.connection.RDFConnectionModular;
import org.aksw.jenax.arq.connection.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.connection.query.QueryEngineFactoryProvider;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.connection.update.UpdateEngineFactoryProvider;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;

/**
 * The default implementation of {@link DatasetRDFConnectionFactory}.
 * Use {@link DatasetRDFConnectionFactoryBuilder} to construct instances.
 *
 */
public class DatasetRDFConnectionFactoryImpl
    implements DatasetRDFConnectionFactory
{
    protected QueryEngineFactoryProvider queryEngineFactoryProvider;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider;

    // Use a context supplier?
    protected Context context;

    public DatasetRDFConnectionFactoryImpl(
            Context context,
            QueryEngineFactoryProvider queryEngineFactoryProvider,
            UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.context = context;
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
    }

    @Override
    public RDFConnection connect(Dataset dataset) {

        Supplier<UpdateExecBuilder> updateExecBuilderSupp =
                () -> new UpdateExecDatasetBuilderEx(updateEngineFactoryProvider).dataset(dataset.asDatasetGraph()).context(context);

        // The update link/connection almost uses the new API
        LinkSparqlUpdate updateLink = new LinkSparqlUpdateOverBuilder(updateExecBuilderSupp);
        RDFLinkModular modularLink = new RDFLinkModular(null, updateLink, null);
        RDFConnection updateConn = RDFConnectionAdapter.adapt(modularLink);

        // The query one still uses the old one
        RDFConnection result = new RDFConnectionModular(
            new SparqlQueryConnectionJsaBase<>(
                new QueryExecutionFactoryDataset(dataset, context, queryEngineFactoryProvider),
                dataset),

            updateConn,
//            new SparqlUpdateConnectionJsaBase<>(
//                new UpdateProcessorFactoryDataset(dataset, context, updateEngineFactoryProvider),
//                dataset),

            RDFConnectionFactory.connect(dataset)
        );

        return result;
    }

}