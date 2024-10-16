package org.aksw.jenax.ron;

import org.apache.jena.graph.Node;

public interface RdfElementNode
    extends RdfElement
{
    Node getInternalId();
}
