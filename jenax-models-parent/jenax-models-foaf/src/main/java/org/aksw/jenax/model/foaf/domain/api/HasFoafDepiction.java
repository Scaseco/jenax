package org.aksw.jenax.model.foaf.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;

@ResourceView
public interface HasFoafDepiction
    extends Resource
{
    @IriType
    @IriNs(FOAF.NS)
    String getDepiction();
    HasFoafDepiction setDepiction(String url);
}
