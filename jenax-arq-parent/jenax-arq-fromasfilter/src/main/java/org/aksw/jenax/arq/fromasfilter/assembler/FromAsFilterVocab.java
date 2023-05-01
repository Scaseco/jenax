package org.aksw.jenax.arq.fromasfilter.assembler;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class FromAsFilterVocab {
    public static final String NS = FromAsFilterTerms.NS;

    public static String getURI() { return NS; }

    public static final Resource DatasetFromAsFilter = ResourceFactory.createResource(NS + "DatasetFromAsFilter");
    public static final Property baseDataset = ResourceFactory.createProperty(FromAsFilterTerms.baseDataset);

}
