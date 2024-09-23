package org.aksw.jenax.arq.util.io;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/** Mixin for RDF data generators that produce dataset graphs. */
public interface ToDatasetGraph {
    DatasetGraph toDatasetGraph(DatasetGraph datasetGraph);

    default DatasetGraph toDatasetGraph() {
        return toDatasetGraph(DatasetGraphFactory.create());
    }

    default Dataset toDataset() {
        return DatasetFactory.wrap(toDatasetGraph());
    }

    default Dataset toDataset(Dataset dataset) {
        toDatasetGraph(dataset.asDatasetGraph());
        return dataset;
    }
}
