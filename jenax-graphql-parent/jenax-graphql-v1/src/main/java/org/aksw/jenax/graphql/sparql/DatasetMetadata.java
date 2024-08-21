package org.aksw.jenax.graphql.sparql;

import java.util.Arrays;
import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
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

public class DatasetMetadata {
    private static final Logger logger = LoggerFactory.getLogger(DatasetMetadata.class);

    protected static final Query classPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/class-partitions.rq");
    protected static final Query propertyPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/property-partitions.rq");
    protected static final List<Query> shaclQueries = SparqlStmtMgr.loadQueries("sh-scalar-properties.rq");

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

    public static ListenableFuture<String> fetchDatasetHash(RdfDataSource dataSource, ListeningExecutorService executorService) {
        ListenableFuture<String> datasetHashFuture = executorService.submit(() -> RdfDataSources.fetchDatasetHash(dataSource));
        return datasetHashFuture;
    }

    public static ListenableFuture<DatasetMetadata> fetch(RdfDataSource dataSource, ListeningExecutorService executorService) {
        QueryExecutionFactoryQuery qef = dataSource.asQef();

        List<Query> voidQueries = Arrays.asList(classPartitionsQuery, propertyPartitionsQuery);
        ListenableFuture<Model> voidModelFuture = asyncModel(executorService, qef, voidQueries);
        ListenableFuture<Model> shaclModelFuture = asyncModel(executorService, qef, shaclQueries);

        ListenableFuture<DatasetMetadata> result =
                Futures.whenAllSucceed(voidModelFuture, shaclModelFuture).call(() -> {
                    List<VoidDataset> voidDatasets = VoidUtils.listVoidDatasets(voidModelFuture.get());
                    VoidDataset voidDataset = IterableUtils.expectZeroOrOneItems(voidDatasets);

                    // If no void dataset was obtained (e.g. because the data graph was empty) then
                    // create an empty resource
                    if (voidDataset == null) {
                        voidDataset = ModelFactory.createDefaultModel().createResource().as(VoidDataset.class);
                    }

                    return new DatasetMetadata(voidDataset, shaclModelFuture.get());
                }
                , executorService);

        return result;
    }
}
