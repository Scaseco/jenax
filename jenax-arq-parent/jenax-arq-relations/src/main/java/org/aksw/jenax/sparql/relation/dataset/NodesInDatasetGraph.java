package org.aksw.jenax.sparql.relation.dataset;

import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;

public interface NodesInDatasetGraph {
    DatasetGraph getDatasetGraph();
    Fragment2 getGraphAndNodeSelector();

    Table listGraphAndNodes();
}
