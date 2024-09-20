package org.aksw.jenax.model.udf.api;

import org.aksw.jenax.norse.term.udf.NorseTermsUdf;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class UdfVocab {
    public static final String NS = NorseTermsUdf.NS;

    public static final Resource UserDefinedFunction = ResourceFactory.createResource(NorseTermsUdf.UserDefinedFunction);
    public static final Property profile = ResourceFactory.createProperty(NorseTermsUdf.profile);
}
