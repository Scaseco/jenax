package org.aksw.jenax.graphql.sparql;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.api.GraphQlExec;
import org.aksw.jenax.graphql.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.api.GraphQlExecFactoryDocument;
import org.aksw.jenax.graphql.impl.common.GraphQlExecFactoryFront;
import org.aksw.jenax.graphql.impl.common.GraphQlResolverAlwaysFail;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import graphql.language.Document;
import graphql.language.Value;

public class GraphQlExecFactoryOverSparql
    implements GraphQlExecFactoryDocument
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryOverSparql.class);

    protected RdfDataSource dataSource;
    protected GraphQlToSparqlConverter converter;

    public GraphQlExecFactoryOverSparql(RdfDataSource dataSource, GraphQlToSparqlConverter converter) {
        super();
        this.dataSource = dataSource;
        this.converter = converter;
    }

    public static GraphQlExecFactory of(RdfDataSource dataSource, GraphQlToSparqlConverter converter) {
        return GraphQlExecFactoryFront.of(new GraphQlExecFactoryOverSparql(dataSource, converter));
    }

    @Override
    public GraphQlExec create(Document document, Map<String, Value<?>> assignments) {
        GraphQlToSparqlMapping mapping = converter.convertDocument(document, assignments);
        GraphQlExec result = new GraphQlExecImpl(dataSource, mapping);
        return result;
    }

    /**
     * Create a GraphQlExecFactory where queries must be fully qualified.
     * This means that any request to resolve a field to a class or property IRI will
     * cause the query to fail.
     */
    public static GraphQlExecFactory of(RdfDataSource dataSource) {
        return of(dataSource, new GraphQlResolverAlwaysFail());
    }

    public static GraphQlExecFactory of(RdfDataSource dataSource, GraphQlResolver resolver) {
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        return of(dataSource, converter);
    }

    /** Create a GraphQlResolver from a DatasetMetadata instance. */
    public static GraphQlResolver resolverOf(DatasetMetadata metadata) {
        return new GraphQlResolverImpl(metadata.getVoidDataset(), metadata.getShaclModel());
    }

    public static GraphQlExecFactory of(RdfDataSource dataSource, DatasetMetadata metadata) {
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        GraphQlExecFactory result = GraphQlExecFactoryFront.of(new GraphQlExecFactoryOverSparql(dataSource, converter));
        return result;
    }

    /** Summarize the data in the data source and configure a resolver with it */
    public static GraphQlExecFactory autoConfEager(RdfDataSource dataSource) {
        DatasetMetadata metadata = Futures.getUnchecked(DatasetMetadata.fetch(dataSource, MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService())));
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        return GraphQlExecFactoryFront.of(new GraphQlExecFactoryOverSparql(dataSource, converter));
    }

    /**
     * Create a GraphQlExecFactory with a resolver that auto-configures itself on the given data
     * on demand (when the first method is called on it).
     * Any further request made to the resolver while auto configuration is in progress will
     * block until completion.
     */
    public static GraphQlExecFactory autoConfLazy(RdfDataSource dataSource) {
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
                GraphQlResolver s = resolverOf(metadata);
                return s;
            }, executorService);

            r.addListener(() -> executorService.shutdown(), executorService);
            return r;
        });

        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        GraphQlExecFactory result = GraphQlExecFactoryFront.of(new GraphQlExecFactoryOverSparql(dataSource, converter));
        return result;
    }
}
