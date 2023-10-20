package org.aksw.jenax.dataaccess.sparql.factory.dataset.connection;

import java.util.function.Supplier;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.dataaccess.sparql.execution.update.UpdateEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.engine.query.QueryEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateOverBuilder;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.QueryExecutionBuilderAdapter;
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
                () -> new UpdateExecDatasetBuilderEx(updateEngineFactoryProvider)
                    .dataset(dataset.asDatasetGraph())
                    .context(context);

        // The update link/connection almost uses the new API
        LinkSparqlUpdate updateLink = new LinkSparqlUpdateOverBuilder(updateExecBuilderSupp);
        RDFLinkModular modularLink = new RDFLinkModular(null, updateLink, null);
        RDFConnection updateConn = RDFConnectionAdapter.adapt(modularLink);


        // The query one still uses the old one
        RDFConnection result = new RDFConnectionModular(
            new SparqlQueryConnectionJsaBase<>(
                new QueryExecutionFactoryDataset(dataset, context, queryEngineFactoryProvider),
                dataset) {

                public QueryExecutionBuilder newQuery() {
                    QueryExecDatasetBuilder core = QueryExecDatasetBuilder.create().dataset(dataset.asDatasetGraph());
                    QueryExecBuilder adapter =  new QueryExecBuilderWrapperBase(core) {
                        public QueryExec build() {

                            QueryExecDatasetBuilder delegate = (QueryExecDatasetBuilder)getDelegate();
                            Query query = delegate.getQuery();

                            query.setResultVars() ;
                            if ( context == null )
                                context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext.
                            DatasetGraph dsg = null ;
                            if ( dataset != null )
                                dsg = dataset.asDatasetGraph() ;
                            QueryEngineFactory f = queryEngineFactoryProvider.find(query, dsg, context);
                            if ( f == null )
                            {
                                Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
                                return null ;
                            }

                            // Merge the contexts
                            Context cxt = Context.setupContextForDataset(context, dsg);

                            //dataset.begin(ReadWrite.WRITE);
                            // FIXME timeout and initial binding need to be passed from the query builder to the qExec
                            QueryExec qExec = new QueryExecDataset(query, query.toString(), dsg, cxt, f, -1, null, -1, null, null) {};

                            return qExec;
                        };
                    };

                    return QueryExecutionBuilderAdapter.adapt(adapter);
                };

            },

            updateConn,
//            new SparqlUpdateConnectionJsaBase<>(
//                new UpdateProcessorFactoryDataset(dataset, context, updateEngineFactoryProvider),
//                dataset),

            RDFConnectionFactory.connect(dataset)
        );

        return result;
    }

}
