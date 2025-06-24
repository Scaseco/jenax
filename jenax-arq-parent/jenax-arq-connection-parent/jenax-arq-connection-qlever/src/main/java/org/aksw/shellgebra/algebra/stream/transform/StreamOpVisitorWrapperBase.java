package org.aksw.shellgebra.algebra.stream.transform;

import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;

public class StreamOpVisitorWrapperBase<T>
    implements StreamOpVisitorWrapper<T>
{
    protected StreamOpVisitor<T> delegate;

    public StreamOpVisitorWrapperBase(StreamOpVisitor<T> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public StreamOpVisitor<T> getDelegate() {
        return delegate;
    }
}
