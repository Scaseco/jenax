package org.aksw.jenax.arq.util.io;

import org.apache.jena.riot.system.StreamRDF;

public class StreamRDFWrapperBase
    implements StreamRDFWrapper
{
    protected StreamRDF delegate;

    public StreamRDFWrapperBase(StreamRDF delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public StreamRDF getDelegate() {
        return delegate;
    }
}
