package org.aksw.jenax.model.ron;

import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@Namespace("https://w3id.org/aksw/norse#ron.")
@ResourceView
public interface RonElement
    extends Resource
{
    boolean isObject();
    boolean isArray();
    // boolean isLiteral();
}
