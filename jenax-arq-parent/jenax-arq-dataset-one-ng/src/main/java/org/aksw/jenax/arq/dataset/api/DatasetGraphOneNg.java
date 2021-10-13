package org.aksw.jenax.arq.dataset.api;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

/** A dataset graph with only a single graph */
public interface DatasetGraphOneNg
    extends DatasetGraph
{
    /** Return the node of the single graph that acts as its name */
    Node getGraphNode();
}
