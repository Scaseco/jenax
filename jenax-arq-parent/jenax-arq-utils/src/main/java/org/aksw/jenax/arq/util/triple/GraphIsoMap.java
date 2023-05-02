package org.aksw.jenax.arq.util.triple;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.common.collect.BiMap;

public interface GraphIsoMap
    extends Graph
{
    // The underlying graph without the variable substitutions
    Graph getWrapped();

    BiMap<Node, Node> getOutToIn();
    BiMap<Node, Node> getInToOut();
}
