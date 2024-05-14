package org.aksw.jenax.arq.util.update;

import org.apache.jena.sparql.modify.request.UpdateVisitor;

public class UpdateVisitorWrapperBase<T extends UpdateVisitor>
    implements UpdateVisitorWrapper<T>
{
    protected T delegate;

    public UpdateVisitorWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}
