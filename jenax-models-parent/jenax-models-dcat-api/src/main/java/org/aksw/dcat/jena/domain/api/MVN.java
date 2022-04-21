package org.aksw.dcat.jena.domain.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/** Vocabulary for maven entities */
public class MVN {
    public static final Property groupId = ResourceFactory.createProperty(MvnTerms.groupId);
    public static final Property artifactId = ResourceFactory.createProperty(MvnTerms.artifactId);
    public static final Property version = ResourceFactory.createProperty(MvnTerms.version);
    public static final Property type = ResourceFactory.createProperty(MvnTerms.type);
    public static final Property classifier = ResourceFactory.createProperty(MvnTerms.classifier);
}
