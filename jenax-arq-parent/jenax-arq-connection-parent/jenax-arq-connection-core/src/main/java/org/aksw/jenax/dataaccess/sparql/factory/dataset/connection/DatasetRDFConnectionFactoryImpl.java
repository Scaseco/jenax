package org.aksw.jenax.dataaccess.sparql.factory.dataset.connection;

import org.aksw.jenax.dataaccess.sparql.execution.update.UpdateEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.engine.query.QueryEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateWrapperBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
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

    // Maybe this should be a context mutator or a context supplier - rather than a static instance. Maybe ContextAcc?
    // For Context mutators we can use RDFLinkUtils.wrapWithContextMutator()
    protected Context localContext;

    public DatasetRDFConnectionFactoryImpl(
            Context context,
            QueryEngineFactoryProvider queryEngineFactoryProvider,
            UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.localContext = context;
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
    }


    /** If an RDFLink is desired then use RDFLinkAdapter.adapt() on the result of {@link #connect(Dataset)} */
    protected RDFLink connectLink(DatasetGraph dataset) {
        RDFLink baseLink = RDFLink.connect(dataset);

        LinkSparqlQuery queryLink = new LinkSparqlQueryWrapperBase(baseLink) {
            @Override
            public QueryExecBuilder newQuery() {
                return new QueryExecDatasetBuilderEx<>(dataset, queryEngineFactoryProvider).context(localContext);
            }
        };

        LinkSparqlUpdate updateLink = new LinkSparqlUpdateWrapperBase(baseLink) {
            @Override
            public UpdateExecBuilder newUpdate() {
                return new UpdateExecDatasetBuilderEx<>(dataset, updateEngineFactoryProvider).context(localContext);
            }
        };

        RDFLink result = new RDFLinkModular(queryLink, updateLink, baseLink);
        return result;
    }

    @Override
    public RDFConnection connect(Dataset dataset) {
        RDFLink link = connectLink(dataset.asDatasetGraph());
        RDFConnection result = RDFConnectionAdapter.adapt(link);
        return result;
    }

}
