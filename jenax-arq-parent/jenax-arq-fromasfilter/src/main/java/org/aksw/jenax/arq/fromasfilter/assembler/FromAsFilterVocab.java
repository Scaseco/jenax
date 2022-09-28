package org.aksw.jenax.arq.fromasfilter.assembler;

import org.apache.jena.assembler.JA;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class FromAsFilterVocab {
    public static final String NS = "http://jena.apache.org/from-enhancer#";

    public static String getURI() { return NS; }

    public static final Resource DatasetFromAsFilter = ResourceFactory.createResource(NS + "DatasetFromAsFilter");
    public static final Property baseDataset = ResourceFactory.createProperty(JA.getURI() + "baseDataset");

}
