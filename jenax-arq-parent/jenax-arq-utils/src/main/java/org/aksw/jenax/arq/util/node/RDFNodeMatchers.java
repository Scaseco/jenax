package org.aksw.jenax.arq.util.node;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public class RDFNodeMatchers {
    public static <T extends RDFNode> RDFNodeMatcher<T> matchSubjectsWithProperty(Class<T> outClass, Property property) {
        return model -> model.listSubjectsWithProperty(property).mapWith(n -> n.as(outClass));
    }
}
