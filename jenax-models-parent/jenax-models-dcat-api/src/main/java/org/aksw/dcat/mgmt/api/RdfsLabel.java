package org.aksw.dcat.mgmt.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

@ResourceView
public interface RdfsLabel
    extends Resource
{
    @IriNs(RDFS.uri)
    String getLabel();
    RdfsLabel setLabel(String label);
}
