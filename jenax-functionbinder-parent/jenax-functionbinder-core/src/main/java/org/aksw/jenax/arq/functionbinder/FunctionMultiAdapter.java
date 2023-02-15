package org.aksw.jenax.arq.functionbinder;

import java.util.List;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * An bridge from a Jena function to a set of Java functions.
 * The fitting Java function is resolved upon function call based on
 * the argument types.
 */
public class FunctionMultiAdapter
    implements Function
{
    protected List<FunctionAdapter> candidates;

    public FunctionMultiAdapter(List<FunctionAdapter> candidates) {
        super();
        this.candidates = candidates;
    }

    @Override
    public void build(String uri, ExprList args) {
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        NodeValue result = null;
        for (FunctionAdapter candidate : candidates) {
            try {
                result = candidate.exec(binding, args, uri, env);
            } catch (Throwable e) {
                // Try next candidate
            }
        }
        if (result == null) {
            throw new RuntimeException("No candidate functions found for " + uri);
        }

        return result;
    }
}
