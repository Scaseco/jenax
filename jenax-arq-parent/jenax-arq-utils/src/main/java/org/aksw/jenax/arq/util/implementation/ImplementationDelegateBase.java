package org.aksw.jenax.arq.util.implementation;

import org.apache.jena.enhanced.Implementation;

public class ImplementationDelegateBase
    extends ImplementationDelegate
{
    protected Implementation delegate;

    public ImplementationDelegateBase(Implementation delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected Implementation getDelegate() {
        return delegate;
    }
}
