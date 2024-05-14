package org.aksw.jenax.facete.model.config;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface CustomFacet
    extends Resource
{
    Node getFacetDirNode();
    Node getFacet();
    // FacetDirNode facetDirNode, Node facet
}
