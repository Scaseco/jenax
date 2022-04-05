package org.aksw.jenax.arq.dataset.impl;

import org.aksw.jenax.arq.dataset.api.DatasetGraphOneNg;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.graph.GraphFactory;

public class DatasetOneNgImpl
    extends DatasetImpl
    implements DatasetOneNg
{
    public DatasetOneNgImpl(DatasetGraphOneNg dsg) {
        super(dsg);
    }

    public static DatasetOneNg wrap(DatasetGraphOneNg dsg) {
        return new DatasetOneNgImpl(dsg);
    }


    public static DatasetOneNg create(Dataset dataset, String graphName) {
        return create(dataset, NodeFactory.createURI(graphName));
    }

    public static DatasetOneNg create(Dataset dataset, Node graphName) {
        DatasetGraphOneNg ng = DatasetGraphOneNgImpl.create(dataset.asDatasetGraph(), graphName);
        return wrap(ng);
    }


    public static DatasetOneNg create(String graphName) {
        return create(graphName, GraphFactory.createDefaultGraph());
    }

    public static DatasetOneNg create(String graphName, Graph graph) {
        return wrap(DatasetGraphOneNgImpl.create(NodeFactory.createURI(graphName), graph));
    }

    @Override
    public String getGraphName() {
        return ((DatasetGraphOneNg)dsg).getGraphNode().getURI();
    }
}
