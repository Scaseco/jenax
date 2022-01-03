package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class RPIF {
    public static final Property resourceUrl = ResourceFactory.createProperty(RpifTerms.NS + "resourceUrl");

    public static final Property op = ResourceFactory.createProperty(RpifTerms.op);
    public static final Property targetBaseName = ResourceFactory.createProperty(RpifTerms.targetBaseName);
    public static final Property targetFileName = ResourceFactory.createProperty(RpifTerms.targetFileName);
    public static final Property targetContentType = ResourceFactory.createProperty(RpifTerms.targetContentType);
    public static final Property targetEncoding = ResourceFactory.createProperty(RpifTerms.targetEncoding);
}
