package org.aksw.jenax.dataaccess.sparql.link.builder;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkModularTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;

public abstract class RDFLinkBuilderBase<X extends RDFLinkBuilderBase<X>>
    implements RDFLinkBuilder<X>
{
    protected RDFLinkModularTransformBuilder linkTransformBuilder = new RDFLinkModularTransformBuilder();

    @Override
    public X linkTransform(RDFLinkTransform linkTransform) {
        linkTransformBuilder.add(linkTransform);
        return self();
    }

    @Override
    public RDFLink build() {
        RDFLink base = buildBaseLink();
        RDFLinkTransform linkTransform = linkTransformBuilder.build();
        RDFLink result = (linkTransform == null)
            ? base
            : linkTransform.apply(base);
        return result;
    }

    public abstract RDFLink buildBaseLink();
}
