package org.aksw.jenax.io.rdf.jsonld.domain;

import org.apache.jena.graph.Node;

public interface FieldCxt {
    String getName();
    void declareProperty(Node property, boolean isForward);

}
