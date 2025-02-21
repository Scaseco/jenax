package org.aksw.jenax.dataaccess.sparql.link.dataset;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.TransformList;
import org.apache.jena.rdflink.LinkDatasetGraph;

/** Composite LinkSparqlQueryTransform */
public final class LinkDatasetGraphTransformList
    extends TransformList<LinkDatasetGraph, LinkDatasetGraphTransform>
    implements LinkDatasetGraphTransform
{
    public LinkDatasetGraphTransformList(List<LinkDatasetGraphTransform> mods) {
        super(mods);
    }
}
