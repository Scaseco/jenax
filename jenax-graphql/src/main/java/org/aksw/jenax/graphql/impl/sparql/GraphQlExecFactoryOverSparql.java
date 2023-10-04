package org.aksw.jenax.graphql.impl.sparql;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecFactoryLazy;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import graphql.language.Document;

public class GraphQlExecFactoryOverSparql
    implements GraphQlExecFactory
{
    // private static final Logger logger = LoggerFactory.getLogger(GraphQlExecFactoryOverSparql.class);

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

    public static GraphQlExecFactory lazyAutoConf(RdfDataSource dataSource) {
        return GraphQlExecFactoryLazy.of(() -> {
            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                    MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));

            ListenableFuture<DatasetMetadata> metadataFuture = DatasetMetadata.fetch(dataSource, executorService);
            ListenableFuture<GraphQlExecFactory> r = Futures.transform(metadataFuture, metadata -> of(dataSource, metadata), executorService);

            r.addListener(() -> executorService.shutdown(), executorService);
            return r;
        });
    }
}
