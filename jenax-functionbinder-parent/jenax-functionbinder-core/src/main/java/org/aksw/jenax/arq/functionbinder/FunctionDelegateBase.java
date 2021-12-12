package org.aksw.jenax.arq.functionbinder;

import org.apache.jena.sparql.function.Function;

public class FunctionDelegateBase
    implements FunctionDelegate
{
    protected Function delegate;

    public FunctionDelegateBase(Function delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Function getDelegate() {
        return delegate;
    }
}
