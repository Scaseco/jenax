package org.aksw.jenax.model.ron;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.RDFNode;


// @Namespace("https://w3id.org/aksw/norse#ron.")
@ResourceView
public interface RonArray
    extends RonElement
{
    List<RDFNode> getMembers();

}
