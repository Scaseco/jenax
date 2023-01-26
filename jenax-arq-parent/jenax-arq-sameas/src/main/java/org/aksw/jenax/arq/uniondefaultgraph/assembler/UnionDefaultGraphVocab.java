package org.aksw.jenax.arq.uniondefaultgraph.assembler;

import org.aksw.jenax.arq.sameas.assembler.SameAsTerms;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class UnionDefaultGraphVocab {
    public static final String NS = "http://jenax.aksw.org/plugin#";

    public static String getURI() { return NS; }

    public static final Resource DatasetUnionDefaultGraph = ResourceFactory.createResource(NS + "DatasetUnionDefaultGraph");
    public static final Resource DatasetAutoUnionDefaultGraph = ResourceFactory.createResource(NS + "DatasetAutoUnionDefaultGraph");
    public static final Property baseDataset = ResourceFactory.createProperty(SameAsTerms.baseDataset);
}
