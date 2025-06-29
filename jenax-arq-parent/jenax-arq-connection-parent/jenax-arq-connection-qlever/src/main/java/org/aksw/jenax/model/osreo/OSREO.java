package org.aksw.jenax.model.osreo;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/** Operating System Runtime Environment Ontology (OSREO) Vocabulary */
public class OSREO {
    public static final String NS = OsreoTerms.O;

    public static final Resource Shell = ResourceFactory.createResource(OsreoTerms.Shell);
    public static final Resource LocatorComand = ResourceFactory.createResource(OsreoTerms.LocatorCommand);
}
