package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkSourceWrapperWithLinkTransform<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperOverNewLinkBase<X>
{
    protected RDFLinkTransform linkTransform;

    public RDFLinkSourceWrapperWithLinkTransform(X delegate, RDFLinkTransform linkTransform) {
        super(delegate);
        this.linkTransform = Objects.requireNonNull(linkTransform);
    }

    public RDFLinkTransform getLinkTransform() {
        return linkTransform;
    }

    @Override
    public RDFLink buildLink() {
        RDFLink base = getDelegate().newLink();
        RDFLink result = linkTransform.apply(base);
        return result;
    }

    @Override
    public String toString() {
        return "RDFLinkSourceWrapperWithLinkTransform [linkTransform=" + linkTransform + ", delegate=" + getDelegate() + "]";
    }
}
