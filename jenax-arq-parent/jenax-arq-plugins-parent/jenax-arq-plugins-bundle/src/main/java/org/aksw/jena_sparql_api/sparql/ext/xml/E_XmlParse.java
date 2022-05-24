package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_XmlParse
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue nv) {
	    NodeValue result;
	    try {
	        result = JenaXmlUtils.resolve(nv);
	    } catch (Exception e) {
	    	throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
	    }

	    return result;
	}
}
