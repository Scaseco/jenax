package org.aksw.jenax.arq.sameas.assembler;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SameAsVocab {
    public static final String NS = SameAsTerms.NS;

    public static String getURI() { return NS; }

    public static final Resource DatasetSameAs = ResourceFactory.createResource(NS + "DatasetSameAs");
    public static final Property baseDataset = ResourceFactory.createProperty(SameAsTerms.baseDataset);

}
