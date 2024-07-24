package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

public interface RdfElementResource
    extends RdfElementNode
{
    Node getExternalId();
}
