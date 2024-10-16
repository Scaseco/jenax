package org.aksw.jenax.io.rdf.jsonld;

import org.apache.jena.graph.Node;

public interface JsonLdCxtAccessor {
    /** Allocate  context object for a field. */
    void getFieldContext(Object cxt, String fieldName);

    /** Declare a field-to-property mapping in the given field context. */
    void declareProperty(Object fieldCxt, Node property, boolean isForward);

    /** Declare a namespace in the given context. */
    void declareNamespace(Object cxt, String prefix, String iri);
}
