package org.aksw.jenax.dataaccess.sparql.link.query;

import java.util.List;

import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.rdflink.LinkSparqlQuery;

/** Composite LinkSparqlQueryTransform */
public final class LinkSparqlQueryTransformList
    extends TransformList<LinkSparqlQuery, LinkSparqlQueryTransform>
    implements LinkSparqlQueryTransform
{
    public LinkSparqlQueryTransformList(List<LinkSparqlQueryTransform> transforms) {
        super(transforms);
    }
}
