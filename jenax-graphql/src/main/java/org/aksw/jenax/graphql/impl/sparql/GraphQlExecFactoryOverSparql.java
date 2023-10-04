package org.aksw.jenax.graphql.impl.sparql;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;

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

    public static GraphQlResolver autoConfigureResolver(RdfDataSource dataSource) {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                  MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));

        DatasetMetadata metadata;
        try {
            ListenableFuture<DatasetMetadata> future = DatasetMetadata.fetch(dataSource, executorService);
            metadata = future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
        GraphQlResolver result = new GraphQlResolverImpl(metadata.getVoidDataset(), metadata.getShaclModel());
        return result;
    }

    public static GraphQlExecFactoryOverSparql autoConfigure(RdfDataSource dataSource) {
        GraphQlResolver resolver = autoConfigureResolver(dataSource);
        GraphQlToSparqlConverter converter = new GraphQlToSparqlConverter(resolver);
        return new GraphQlExecFactoryOverSparql(dataSource, converter);
    }

    @Override
    public GraphQlExec create(Document document) {
        GraphQlToSparqlMapping mapping = converter.convertDocument(document);
        GraphQlExec result = new GraphQlExecImpl(dataSource, mapping);
        return result;
    }
}
