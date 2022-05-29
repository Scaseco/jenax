package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.aksw.jenax.arq.util.expr.FunctionBase1WithEnv;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class E_XmlParse
	extends FunctionBase1WithEnv
{
	@Override
	public NodeValue exec(NodeValue nv, FunctionEnv env) {
	    NodeValue result;
	    try {
	        result = JenaXmlUtils.resolve(nv, env);
	    } catch (Exception e) {
	    	throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
	    }

	    return result;
	}
}
