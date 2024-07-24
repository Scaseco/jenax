package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

public interface RdfLiteral
    extends RdfElement
{
    Node getInternalId();
}
