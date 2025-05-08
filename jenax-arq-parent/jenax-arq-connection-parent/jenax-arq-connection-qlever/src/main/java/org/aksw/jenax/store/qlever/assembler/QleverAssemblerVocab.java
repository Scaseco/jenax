package org.aksw.jenax.store.qlever.assembler;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class QleverAssemblerVocab {

    public static final Resource Dataset = ResourceFactory.createResource(QleverAssemblerTerms.Dataset);

    public static final Property location = ResourceFactory.createProperty(QleverAssemblerTerms.location);
    public static final Property indexName = ResourceFactory.createProperty(QleverAssemblerTerms.indexName);
    public static final Property port = ResourceFactory.createProperty(QleverAssemblerTerms.port);
    public static final Property accessToken = ResourceFactory.createProperty(QleverAssemblerTerms.accessToken);
}
