package org.aksw.jenax.graphql.impl.sparql;

import java.util.Arrays;
import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.arq.util.exec.QueryExecutionUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.aksw.jenax.model.voidx.util.VoidUtils;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class DatasetMetadata {
    protected static final Query datasetHashQuery = SparqlStmtMgr.loadQuery("probe-dataset-hash-simple.rq");
    protected static final Query classPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/class-partitions.rq");
    protected static final Query propertyPartitionsQuery = SparqlStmtMgr.loadQuery("void/minimal/property-partitions.rq");
    protected static final List<Query> shaclQueries = SparqlStmtMgr.loadQueries("sh-scalar-properties.rq");

    protected String datasetHash;
    protected VoidDataset voidDataset;
    protected Model shaclModel;

    public DatasetMetadata(String datasetHash, VoidDataset voidDataset, Model shaclModel) {
        super();
        this.datasetHash = datasetHash;
        this.voidDataset = voidDataset;
        this.shaclModel = shaclModel;
    }

    public String getDatasetHash() {
        return datasetHash;
    }

    public VoidDataset getVoidDataset() {
        return voidDataset;
    }

    public Model getShaclModel() {
        return shaclModel;
    }

    public static String fetchDatasetHash(QueryExecutionFactoryQuery qef) {
        String result = QueryExecutionUtils.fetchNode(qef::createQueryExecution, datasetHashQuery)
                .map(Node::getLiteralLexicalForm)
                .map(String::toLowerCase)
                .orElse(null);
        return result;
    }

    public static Model combine(List<Model> models) {
        Model result = ModelFactory.createDefaultModel();
        models.stream().forEach(result::add);
        return result;
    }

    public static ListenableFuture<Model> asyncModel(ListeningExecutorService executorService, QueryExecutionFactoryQuery qef, List<Query> constructQueries) {
        AsyncCombiner<Model, Model> combiner = AsyncCombiner.of(executorService, DatasetMetadata::combine);
        for (Query query : constructQueries) {
            combiner.addTask(() -> qef.execConstruct(query));
        }
        return combiner.exec();
    }

    public static ListenableFuture<DatasetMetadata> fetch(RdfDataSource dataSource, ListeningExecutorService executorService) {
        QueryExecutionFactoryQuery qef = dataSource.asQef();

        List<Query> voidQueries = Arrays.asList(classPartitionsQuery, propertyPartitionsQuery);
        ListenableFuture<String> datasetHashFuture = executorService.submit(() -> fetchDatasetHash(qef));
        ListenableFuture<Model> voidModelFuture = asyncModel(executorService, qef, voidQueries);
        ListenableFuture<Model> shaclModelFuture = asyncModel(executorService, qef, shaclQueries);

        ListenableFuture<DatasetMetadata> result =
                Futures.whenAllSucceed(datasetHashFuture, voidModelFuture, shaclModelFuture).call(() -> {
                    List<VoidDataset> voidDatasets = VoidUtils.listVoidDatasets(voidModelFuture.get());
                    VoidDataset voidDataset = IterableUtils.expectZeroOrOneItems(voidDatasets);
                    return new DatasetMetadata(datasetHashFuture.get(), voidDataset, shaclModelFuture.get());
                }
                , executorService);

        return result;
    }
}
