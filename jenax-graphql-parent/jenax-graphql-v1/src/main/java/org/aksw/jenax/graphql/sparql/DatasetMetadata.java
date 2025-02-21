package org.aksw.jenax.graphql.sparql;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.aksw.jenax.model.voidx.util.VoidUtils;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class DatasetMetadata {
    private static final Logger logger = LoggerFactory.getLogger(DatasetMetadata.class);

    protected static final Query classPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/class-partitions.rq");
    protected static final Query propertyPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/property-partitions.rq");
    public static final List<Query> defaultVoidQueries = List.of(classPartitionsQuery, propertyPartitionsQuery);

    public static final List<Query> defaultShaclQueries = List.copyOf(SparqlStmtMgr.loadQueries("sh-scalar-properties.rq"));


    protected VoidDataset voidDataset;
    protected Model shaclModel;

    public DatasetMetadata(VoidDataset voidDataset, Model shaclModel) {
        super();
        this.voidDataset = voidDataset;
        this.shaclModel = shaclModel;
    }

    public VoidDataset getVoidDataset() {
        return voidDataset;
    }

    public Model getShaclModel() {
        return shaclModel;
    }

    public static Model combine(List<Model> models) {
        Model result = ModelFactory.createDefaultModel();
        models.stream().forEach(result::add);
        return result;
    }

    public static ListenableFuture<Model> asyncModel(ListeningExecutorService executorService, QueryExecutionFactoryQuery qef, List<Query> constructQueries) {
        AsyncCombiner<Model, Model> combiner = AsyncCombiner.of(executorService, DatasetMetadata::combine);
        for (Query query : constructQueries) {
            combiner.addTask(() -> {
                Model r;
                try {
                    r = qef.execConstruct(query);
                } catch (Exception e) {
                    logger.info("Query execution failed: " + query);
                    throw new RuntimeException(e);
                }
                return r;
            });
        }
        return combiner.exec();
    }

    public static ListenableFuture<String> fetchDatasetHash(RDFDataSource dataSource, ListeningExecutorService executorService) {
        ListenableFuture<String> datasetHashFuture = executorService.submit(() -> RdfDataSources.fetchDatasetHash(dataSource));
        return datasetHashFuture;
    }

    public static DatasetMetadata of(Model voidModel, Model shaclModel) {
        List<VoidDataset> voidDatasets = VoidUtils.listVoidDatasets(voidModel);
        VoidDataset voidDataset = IterableUtils.expectZeroOrOneItems(voidDatasets);
        if (voidDataset == null) {
            voidDataset = ModelFactory.createDefaultModel().createResource().as(VoidDataset.class);
        }
        return new DatasetMetadata(voidDataset, shaclModel);
    }

    public static DatasetMetadata fetch(RDFDataSource dataSource) {
        return fetch(dataSource, defaultVoidQueries, defaultShaclQueries);
    }

    public static ListenableFuture<DatasetMetadata> fetch(RDFDataSource dataSource, ListeningExecutorService executorService) {
        List<Query> voidQueries = Arrays.asList(classPartitionsQuery, propertyPartitionsQuery);
        return fetch(dataSource, executorService, voidQueries, defaultShaclQueries);
    }

    public static DatasetMetadata fetch(RDFDataSource dataSource, List<Query> voidQueries, List<Query> shaclQueries) {
        if (voidQueries == null) {
            voidQueries = defaultVoidQueries;
        }

        if (shaclQueries == null) {
            shaclQueries = defaultShaclQueries;
        }

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool()));
        DatasetMetadata result;
        try {
            ListenableFuture<DatasetMetadata> future = fetch(dataSource, executorService, voidQueries, shaclQueries);
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static ListenableFuture<DatasetMetadata> fetch(RDFDataSource dataSource, ListeningExecutorService executorService,
            List<Query> voidQueries, List<Query> shaclQueries) {
        QueryExecutionFactoryQuery qef = dataSource.asQef();

        ListenableFuture<Model> voidModelFuture = asyncModel(executorService, qef, voidQueries);
        ListenableFuture<Model> shaclModelFuture = asyncModel(executorService, qef, shaclQueries);

        ListenableFuture<DatasetMetadata> result =
                Futures.whenAllSucceed(voidModelFuture, shaclModelFuture).call(() -> {
                    Model voidModel = voidModelFuture.get();
                    Model shaclModel = shaclModelFuture.get();
                    return DatasetMetadata.of(voidModel, shaclModel);
                }
                , executorService);

        return result;
    }
}
