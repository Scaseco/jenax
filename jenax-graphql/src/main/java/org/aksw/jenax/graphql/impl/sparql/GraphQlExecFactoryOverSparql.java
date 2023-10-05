package org.aksw.jenax.graphql.impl.sparql;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecFactoryLazy;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import graphql.language.Document;

public class GraphQlExecFactoryOverSparql
    implements GraphQlExecFactory
{
    private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryOverSparql.class);

    protected RdfDataSource dataSource;
    protected GraphQlToSparqlConverter converter;

    public GraphQlExecFactoryOverSparql(RdfDataSource dataSource, GraphQlToSparqlConverter converter) {
        super();
        this.dataSource = dataSource;
        this.converter = converter;
    }

//    public static ListenableFuture<DatasetMetadata> autoConfigureMetaData(RdfDataSource dataSource) {
//        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
//                  MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));
//
//        ListenableFuture<DatasetMetadata> result = DatasetMetadata.fetch(dataSource, executorService);
//        return result;
//    }

    public static GraphQlResolver resolverOf(DatasetMetadata metadata) {
        return new GraphQlResolverImpl(metadata.getVoidDataset(), metadata.getShaclModel());
    }

    public static GraphQlExecFactory of(RdfDataSource dataSource, DatasetMetadata metadata) {
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        GraphQlExecFactory result = new GraphQlExecFactoryOverSparql(dataSource, converter);
        return result;
    }

    public static GraphQlExecFactoryOverSparql autoConfigure(RdfDataSource dataSource) {
        DatasetMetadata metadata = Futures.getUnchecked(DatasetMetadata.fetch(dataSource, MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService())));
        GraphQlResolver resolver = resolverOf(metadata);
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        return new GraphQlExecFactoryOverSparql(dataSource, converter);
    }

    @Override
    public GraphQlExec create(Document document) {
        GraphQlToSparqlMapping mapping = converter.convertDocument(document);
        GraphQlExec result = new GraphQlExecImpl(dataSource, mapping);
        return result;
    }

    /**
     * Create a GraphQlExecFactory that auto-configures itself on first use.
     * Any query made while auto configuration is in progress will block until completion.
     */
    public static GraphQlExecFactory autoConfigureLazy(RdfDataSource dataSource) {
        return GraphQlExecFactoryLazy.of(() -> {
            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                    MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));

            ListenableFuture<DatasetMetadata> metadataFuture = DatasetMetadata.fetch(dataSource, executorService);
            // TODO Make display of auto-detection results optional
            ListenableFuture<GraphQlExecFactory> r = Futures.transform(metadataFuture, metadata -> {
                if (logger.isInfoEnabled()) {
                    Set<Node> classes = metadata.getVoidDataset().getClassPartitionMap().keySet();
                    logger.info("Autodetected classes: " + classes);

                    Set<Node> properties = metadata.getVoidDataset().getPropertyPartitionMap().keySet();
                    logger.info("Autodetected properties: " + properties);
                }

                GraphQlExecFactory s = of(dataSource, metadata);
                return s;
            }, executorService);

            r.addListener(() -> executorService.shutdown(), executorService);
            return r;
        });
    }
}
