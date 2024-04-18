package org.aksw.jenax.arq.functionbinder;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;

public class FunctionWrapperBase
    implements Function
{
    protected Function delegate;

    public FunctionWrapperBase(Function delegate) {
        super();
        this.delegate = delegate;
    }

    public Function getDelegate() {
        return delegate;
    }

    @Override
    public void build(String uri, ExprList args, Context context) {
        delegate.build(uri, args, context);
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        return delegate.exec(binding, args, uri, env);
    }
}
