package org.aksw.jenax.arq.util.node;

import java.util.List;

import org.apache.jena.graph.Node;

/** A list of RDF terms represented as Jena Nodes */
public interface NodeList
    extends NodeCollection, List<Node>
{
}
