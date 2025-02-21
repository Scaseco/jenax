package org.aksw.jenax.dataaccess.sparql.link.transform;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.TransformList;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkTransformList
    extends TransformList<RDFLink, RDFLinkTransform>
    implements RDFLinkTransform
{
    public RDFLinkTransformList(List<RDFLinkTransform> mods) {
        super(mods);
    }
}
