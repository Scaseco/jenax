package org.aksw.jenax.graphql.sparql;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.impl.common.GraphQlResolverAlwaysFail;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecBuilder;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExecFactory;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class GraphQlExecFactoryOverSparql
    implements RdfGraphQlExecFactory
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryOverSparql.class);

    protected RdfDataSource dataSource;
    protected GraphQlToSparqlMappingFactory mappingFactory;

    public GraphQlExecFactoryOverSparql(RdfDataSource dataSource, GraphQlToSparqlMappingFactory mappingFactory) {
        super();
        this.dataSource = dataSource;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public RdfGraphQlExecBuilder newBuilder() {
        return new GraphQlExecBuilderOverSparql(dataSource, mappingFactory);
    }

    public static RdfGraphQlExecFactory of(RdfDataSource dataSource, GraphQlToSparqlMappingFactory mappingFactory) {
        return new GraphQlExecFactoryOverSparql(dataSource, mappingFactory); // GraphQlExecFactoryFront.of(new GraphQlExecFactoryOverSparql(dataSource, converter));
    }

    /**
     * Create a GraphQlExecFactory where queries must be fully qualified.
     * This means that any request to resolve a field to a class or property IRI will
     * cause the query to fail.
     */
    public static RdfGraphQlExecFactory of(RdfDataSource dataSource) {
        return of(dataSource, new GraphQlResolverAlwaysFail());
    }

    public static RdfGraphQlExecFactory of(RdfDataSource dataSource, GraphQlResolver resolver) {
        GraphQlToSparqlMappingFactory mappingFactory = () -> new GraphQlToSparqlMappingBuilderImpl().setResolver(resolver);
        return of(dataSource, mappingFactory);
    }

    /** Create a GraphQlResolver from a DatasetMetadata instance. */
    public static GraphQlResolver resolverOf(DatasetMetadata metadata) {
        return new GraphQlResolverImpl(metadata.getVoidDataset(), metadata.getShaclModel());
    }

    public static RdfGraphQlExecFactory of(RdfDataSource dataSource, DatasetMetadata metadata) {
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlMappingFactory mappingFactory = () -> new GraphQlToSparqlMappingBuilderImpl().setResolver(resolver);
        RdfGraphQlExecFactory result = of(dataSource, mappingFactory);
        return result;
    }

    /** Summarize the data in the data source and configure a resolver with it */
    public static RdfGraphQlExecFactory autoConfEager(RdfDataSource dataSource) {
        DatasetMetadata metadata = Futures.getUnchecked(DatasetMetadata.fetch(dataSource, MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService())));
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlMappingFactory mappingFactory = () -> new GraphQlToSparqlMappingBuilderImpl().setResolver(resolver);
        return of(dataSource, mappingFactory);
    }

    /**
     * Create a GraphQlExecFactory with a resolver that auto-configures itself on the given data
     * on demand (when the first method is called on it).
     * Any further request made to the resolver while auto configuration is in progress will
     * block until completion.
     */
    public static RdfGraphQlExecFactory autoConfLazy(RdfDataSource dataSource) {
        GraphQlResolver resolver = GraphQlResolverImplLazy.of(() -> {
            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                    MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));

            ListenableFuture<DatasetMetadata> metadataFuture = DatasetMetadata.fetch(dataSource, executorService);
            // TODO Make display of auto-detection results optional
            ListenableFuture<GraphQlResolver> r = Futures.transform(metadataFuture, metadata -> {
                if (logger.isInfoEnabled()) {
                    Set<Node> classes = metadata.getVoidDataset().getClassPartitionMap().keySet();
                    logger.info("Autodetected classes: " + classes);

                    Set<Node> properties = metadata.getVoidDataset().getPropertyPartitionMap().keySet();
                    logger.info("Autodetected properties: " + properties);
                }
                GraphQlResolver s = GraphQlExecFactoryOverSparql.resolverOf(metadata);
                return s;
            }, executorService);

            r.addListener(() -> executorService.shutdown(), executorService);
            return r;
        });

        // GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        GraphQlToSparqlMappingFactory mappingFactory = () -> new GraphQlToSparqlMappingBuilderImpl().setResolver(resolver);
        RdfGraphQlExecFactory result = GraphQlExecFactoryOverSparql.of(dataSource, mappingFactory);
        return result;
    }
}
