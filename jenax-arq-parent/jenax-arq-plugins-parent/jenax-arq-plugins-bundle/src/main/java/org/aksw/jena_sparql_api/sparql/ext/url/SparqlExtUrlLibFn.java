package org.aksw.jena_sparql_api.sparql.ext.url;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class SparqlExtUrlLibFn {

    // FIXME FunctionBinder does not yet support binding the FunctionEnv argument.
    public static NodeValue text(NodeValue nv,  FunctionEnv env) {
        NodeValue result;
        try {
            result = JenaUrlUtils.resolve(nv, env);
        } catch (Exception e) {
            throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
        }
        return result;
    }
}
