package org.aksw.jenax.arq.util.binding;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Combines binding and context (a {@link FunctionEnv}) into a single object.
 * In general, both are needed for correctly evaluating {@link Expr} instances.
 *
 * Primarily used to bridge jena and aksw aggregators.
 */
@Deprecated // No longer needed as our Aggregator framework now natively supports context objects such as FunctionEnv
public class BindingEnv {
    protected Binding binding;
    protected FunctionEnv functionEnv;

    public BindingEnv(Binding binding, FunctionEnv functionEnv) {
        super();
        this.binding = binding;
        this.functionEnv = functionEnv;
    }

    public Binding getBinding() {
        return binding;
    }

    public FunctionEnv getFunctionEnv() {
        return functionEnv;
    }
}
