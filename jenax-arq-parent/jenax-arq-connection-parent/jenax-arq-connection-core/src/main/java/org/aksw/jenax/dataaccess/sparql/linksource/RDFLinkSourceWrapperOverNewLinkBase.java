package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverRDFLinkSource;
import org.apache.jena.rdflink.RDFLink;

public abstract class RDFLinkSourceWrapperOverNewLinkBase<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
{
    public RDFLinkSourceWrapperOverNewLinkBase(X delegate) {
        super(delegate);
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        return new RDFLinkBuilderOverRDFLinkSource<>(this);
    }

    @Override
    public RDFLink newLink() {
        RDFLink result = buildLink();
        return result;
    }

    protected abstract RDFLink buildLink();
}
