package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.graph.Node;

public interface RdfElementResource
    extends RdfElementNode
{
    Node getExternalId();
}
