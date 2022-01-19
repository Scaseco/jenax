package org.aksw.dcat.mgmt.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;

@ResourceView
public interface FoafDepiction
    extends Resource
{
    @IriType
    @IriNs(FOAF.NS)
    String getDepiction();
    FoafDepiction setDepiction(String url);
}
