package org.aksw.jenax.dataaccess.sparql.link.builder;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkModularTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTPBuilder;

public class RDFLinkBuilderHTTP<X extends RDFLinkBuilderHTTP<X>>
    extends RDFLinkHTTPBuilder
    implements RDFLinkBuilder<X>
{
    protected RDFLinkModularTransformBuilder linkTransformBuilder = new RDFLinkModularTransformBuilder();

    // XXX There is RDFLinkHTTPBuilder.creator for going from LinkBuilder to Link.
    @Override
    public X linkTransform(RDFLinkTransform linkTransform) {
        linkTransformBuilder.add(linkTransform);
        return self();
    }

    @Override
    public RDFLink build() {
        RDFLink base = super.build();
        RDFLinkTransform linkTransform = linkTransformBuilder.build();
        RDFLink result = (linkTransform == null)
            ? base
            : linkTransform.apply(base);
        return result;
    }
}
