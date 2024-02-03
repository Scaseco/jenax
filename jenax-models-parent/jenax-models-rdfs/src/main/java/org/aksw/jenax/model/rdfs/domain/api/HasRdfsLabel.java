package org.aksw.jenax.model.rdfs.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

/**
 * View for accessing an RDF resource's rdfs:label property.
 *
 * By convention, the prefix "Has" indicates that the interface corresponds
 * to an individual RDF property rather than a class.
 */
@ResourceView
public interface HasRdfsLabel
    extends Resource
{
    @IriNs(RDFS.uri)
    String getLabel();
    HasRdfsLabel setLabel(String label);
}
