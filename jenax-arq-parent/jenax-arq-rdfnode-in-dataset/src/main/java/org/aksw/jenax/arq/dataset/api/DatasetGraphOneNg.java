package org.aksw.jenax.arq.dataset.api;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public interface DatasetGraphOneNg
    extends DatasetGraph
{
    Node getGraphNode();
}
