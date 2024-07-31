package org.aksw.jenax.ron;

import org.apache.jena.graph.Node;

public interface RdfElementResource
    extends RdfElementNode
{
    Node getExternalId();
}
