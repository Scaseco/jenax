package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.graph.Node;

public interface RdfElementNode
    extends RdfElement
{
    Node getInternalId();
}
