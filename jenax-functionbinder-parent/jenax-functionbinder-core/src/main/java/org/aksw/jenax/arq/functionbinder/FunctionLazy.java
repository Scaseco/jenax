package org.aksw.jenax.arq.functionbinder;

import java.util.function.Supplier;

import org.apache.jena.sparql.function.Function;

public class FunctionLazy
    extends FunctionDelegateBase
{
    protected Supplier<Function> delegateSupplier;

    public FunctionLazy(Supplier<Function> delegateSupplier) {
        super(null);
        this.delegateSupplier = delegateSupplier;
    }

    @Override
    public Function getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = delegateSupplier.get();
                }
            }
        }

        return delegate;
    }
}
