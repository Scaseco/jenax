package org.aksw.jenax.model.polyfill.domain.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class PolyfillVocab {
    public static final Property suggestion = ResourceFactory.createProperty(PolyfillTerms.suggestion);
}
