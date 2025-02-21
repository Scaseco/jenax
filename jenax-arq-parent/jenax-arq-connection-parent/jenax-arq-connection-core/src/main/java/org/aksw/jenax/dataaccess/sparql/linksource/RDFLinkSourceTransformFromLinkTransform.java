package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;

public final class RDFLinkSourceTransformFromLinkTransform
    implements RDFLinkSourceTransform
{
    protected RDFLinkTransform linkTransform;

    public RDFLinkSourceTransformFromLinkTransform(RDFLinkTransform linkTransform) {
        super();
        this.linkTransform = Objects.requireNonNull(linkTransform);
    }

    public RDFLinkTransform getLinkTransform() {
        return linkTransform;
    }

    @Override
    public RDFLinkSource apply(RDFLinkSource t) {
        return new RDFLinkSourceWrapperWithLinkTransform<>(t, linkTransform);
    }
}
