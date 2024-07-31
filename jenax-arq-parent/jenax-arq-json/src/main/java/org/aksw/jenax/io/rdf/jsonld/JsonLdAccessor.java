package org.aksw.jenax.io.rdf.jsonld;

import org.apache.jena.graph.Node;

public interface JsonLdAccessor {
    void declareProperty(Object json, String fieldName, Node property, boolean isForward);
    void declareNamespace(Object json, String prefix, String iri);
}
