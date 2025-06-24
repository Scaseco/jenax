package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.List;

import org.aksw.jenax.arq.util.query.TransformList;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;

public class RDFLinkSourceTransformList
    extends TransformList<RDFLinkSource, RDFLinkSourceTransform>
    implements RDFLinkSourceTransform
{
    public RDFLinkSourceTransformList(List<RDFLinkSourceTransform> mods) {
        super(mods);
    }
}
