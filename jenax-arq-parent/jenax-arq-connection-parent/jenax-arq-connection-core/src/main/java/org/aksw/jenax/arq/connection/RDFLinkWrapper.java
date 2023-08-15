package org.aksw.jenax.arq.connection;

import java.util.Objects;

import org.apache.jena.rdflink.RDFLink;

public class RDFLinkWrapper
    implements RDFLinkDelegate
{
    protected RDFLink delegate;

    public RDFLinkWrapper(RDFLink delegate) {
        super();
        Objects.requireNonNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public RDFLink getDelegate() {
        return delegate;
    }
}
