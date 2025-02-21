package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.TransformList;
import org.apache.jena.rdflink.LinkSparqlUpdate;

/** Composite LinkSparqlQueryTransform */
public final class LinkSparqlUpdateTransformList
    extends TransformList<LinkSparqlUpdate, LinkSparqlUpdateTransform>
    implements LinkSparqlUpdateTransform
{
    public LinkSparqlUpdateTransformList(List<LinkSparqlUpdateTransform> mods) {
        super(mods);
    }
}
