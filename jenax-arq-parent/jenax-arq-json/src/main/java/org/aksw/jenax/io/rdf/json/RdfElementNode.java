package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

public interface RdfElementNode
    extends RdfElement
{
    Node getInternalId();
}
