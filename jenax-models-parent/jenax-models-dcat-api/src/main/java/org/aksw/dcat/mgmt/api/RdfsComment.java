package org.aksw.dcat.mgmt.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

@ResourceView
public interface RdfsComment
    extends Resource
{
    @IriType
    @IriNs(RDFS.uri)
    String getComment();
    RdfsComment setComment(String url);
}
