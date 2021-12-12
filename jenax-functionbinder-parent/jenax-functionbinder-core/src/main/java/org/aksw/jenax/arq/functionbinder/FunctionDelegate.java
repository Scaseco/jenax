package org.aksw.jenax.arq.functionbinder;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;

public interface FunctionDelegate
    extends Function
{
    Function getDelegate();

    @Override
    default void build(String uri, ExprList args) {
        getDelegate().build(uri, args);
    }

    @Override
    default NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        return getDelegate().exec(binding, args, uri, env);
    }
}
