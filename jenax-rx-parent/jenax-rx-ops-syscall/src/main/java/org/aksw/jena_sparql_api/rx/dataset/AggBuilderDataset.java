package org.aksw.jena_sparql_api.rx.dataset;

import java.util.Collections;
import java.util.Map;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jenax.arq.util.quad.DatasetGraphUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.function.FunctionEnv;

public class AggBuilderDataset
{
    /** Aggregate all quads into a single dataset */
    public static ParallelAggregator<Quad, FunctionEnv, Dataset, ?> quadsToDataset(SerializableSupplier<? extends DatasetGraph> datasetGraphSupplier) {
        return AggBuilder.fromCollector(
                () -> (DatasetGraph)datasetGraphSupplier.get(),
                DatasetGraph::add,
                DatasetGraphUtils::addAll,
                DatasetFactory::wrap);
    }

    /** Group quads by a key (typically the graph component) and map each to its own dataset */
    public static <K> ParallelAggregator<Quad, FunctionEnv, Map<K, Dataset>, ?> groupQuadsToDatasetCore(
            SerializableSupplier<? extends DatasetGraph> datasetGraphSupplier,
            SerializableFunction<? super Quad, K> keyMapper) {

        return AggBuilder.inputSplit(
                quad -> Collections.singleton(keyMapper.apply(quad)),
                (quad, key) -> quad,
                quadsToDataset(datasetGraphSupplier));
    }
}
