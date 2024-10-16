package org.aksw.jenax.dataaccess.sparql.link.common;

import java.util.Objects;

import org.apache.jena.rdflink.RDFLink;

public class RDFLinkWrapperBase
    implements RDFLinkWrapper
{
    protected RDFLink delegate;

    public RDFLinkWrapperBase(RDFLink delegate) {
        super();
        Objects.requireNonNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public RDFLink getDelegate() {
        return delegate;
    }
}
