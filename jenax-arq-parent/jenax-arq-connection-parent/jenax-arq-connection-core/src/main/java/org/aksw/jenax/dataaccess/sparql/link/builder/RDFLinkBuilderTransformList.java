package org.aksw.jenax.dataaccess.sparql.link.builder;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.TransformList;

public class RDFLinkBuilderTransformList
    extends TransformList<RDFLinkBuilder, RDFLinkBuilderTransform>
    implements RDFLinkBuilderTransform
{
    public RDFLinkBuilderTransformList(List<RDFLinkBuilderTransform> mods) {
        super(mods);
    }

    /**
     * Return null if there are no mods,
     * the only mod if there is just 1,
     * and this if there are more than 1 mods.
     */
    public RDFLinkBuilderTransform compact() {
        return mods.isEmpty()
            ? null
            : mods.size() == 1
                ? mods.iterator().next()
                : this;
    }
}
