package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkSourceWrapperWithLinkTransform<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
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
    public RDFLink newLink() {
        RDFLink base = super.newLink();
        RDFLink result = linkTransform.apply(base);
        return result;
    }
}
