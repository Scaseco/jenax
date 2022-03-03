package org.aksw.difs.sys.vocab.jena;

import org.aksw.difs.sys.vocab.common.DIFSTerms;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class DIFS {
    public static final Property storePath = ResourceFactory.createProperty(DIFSTerms.storePath);
    public static final Property indexPath = ResourceFactory.createProperty(DIFSTerms.indexPath);
    public static final Property index = ResourceFactory.createProperty(DIFSTerms.index);
    public static final Property predicate = ResourceFactory.createProperty(DIFSTerms.predicate);
    public static final Property folder = ResourceFactory.createProperty(DIFSTerms.folder);
    public static final Property method = ResourceFactory.createProperty(DIFSTerms.method);
    public static final Property heartbeatInterval = ResourceFactory.createProperty(DIFSTerms.heartbeatInterval);
}
