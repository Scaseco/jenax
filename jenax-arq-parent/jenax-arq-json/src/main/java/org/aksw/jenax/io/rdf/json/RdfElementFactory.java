package org.aksw.jenax.io.rdf.json;

import org.apache.jena.graph.Node;

public interface RdfElementFactory {
    // Allow of(null) ? Probably not because error prone
    RdfElement nullValue();
    RdfElement of(Node node);
    RdfArray newArray();
}
