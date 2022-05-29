package org.aksw.jena_sparql_api.sparql.ext.url;

import org.aksw.jenax.arq.util.expr.FunctionBase1WithEnv;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class E_UrlText
    extends FunctionBase1WithEnv
{
    @Override
    public NodeValue exec(NodeValue nv, FunctionEnv env)
    {
        NodeValue result;
        try {
            result = JenaUrlUtils.resolve(nv, env);
        } catch (Exception e) {
        	throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
            //throw new RuntimeException(e);
        }

        return result;
    }
}
