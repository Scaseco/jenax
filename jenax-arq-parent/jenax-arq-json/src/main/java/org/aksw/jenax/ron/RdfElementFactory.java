package org.aksw.jenax.ron;

import org.apache.jena.graph.Node;

public interface RdfElementFactory {
    // Allow of(null) ? Probably not because error prone
    RdfNull nullValue();
    RdfLiteral newLiteral(Node node);
    RdfArray newArray();
    RdfObject newObject();
}
