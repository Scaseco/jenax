package org.aksw.jenax.sparql.relation.dataset;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;


public class NodesInDatasetImpl
    implements NodesInDataset
{
    protected Dataset dataset;
    protected Set<GraphNameAndNode> graphNameAndNodes;


    /** For serialization e.g. using gson*/
    NodesInDatasetImpl() {
        this(null, null);
    }

    public NodesInDatasetImpl(Dataset dataset) {
        this(dataset, new LinkedHashSet<>());
    }

    public NodesInDatasetImpl(Dataset dataset, Set<GraphNameAndNode> node) {
        super();
        this.dataset = dataset;
        this.graphNameAndNodes = node;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public Set<GraphNameAndNode> getGraphNameAndNodes() {
        return graphNameAndNodes;
    }

    @Override
    public String toString() {
        return "graphNameAndNodes=" + graphNameAndNodes + ", datasetSize=" + Streams.stream(dataset.asDatasetGraph().find()).count();
    }
}