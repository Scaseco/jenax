package org.aksw.jenax.sparql.relation.dataset;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;

public interface NodesInDatasetGraph {
    DatasetGraph getDatasetGraph();
    BinaryRelation getGraphAndNodeSelector();

    Table listGraphAndNodes();
}
