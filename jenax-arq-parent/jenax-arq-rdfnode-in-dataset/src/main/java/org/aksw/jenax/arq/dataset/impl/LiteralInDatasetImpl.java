package org.aksw.jenax.arq.dataset.impl;

import org.aksw.jenax.arq.dataset.api.LiteralInDataset;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.impl.LiteralImpl;


public class LiteralInDatasetImpl
    extends LiteralImpl
    implements LiteralInDataset
{
    protected Dataset dataset;
    protected String graphName;

    public LiteralInDatasetImpl(Dataset dataset, String graphName, Node node) {
        super(node, (EnhGraph)DatasetUtils.getDefaultOrNamedModel(dataset, graphName));
        this.dataset = dataset;
        this.graphName = graphName;
    }

    @Override
    public String getGraphName() {
        return graphName;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public LiteralInDatasetImpl asLiteral() {
        return this;
    }

    @Override
    public LiteralInDatasetImpl inDataset(Dataset other) {
        return new LiteralInDatasetImpl(other, graphName, node);
    }

    @Override
    public ResourceInDataset asResource() {
        super.asResource(); // Raises an exception
        return null;
    }
}
