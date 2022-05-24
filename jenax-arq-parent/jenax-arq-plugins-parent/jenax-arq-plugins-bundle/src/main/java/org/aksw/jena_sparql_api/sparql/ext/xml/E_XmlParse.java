package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.aksw.jena_sparql_api.sparql.ext.url.E_UrlText;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.common.io.CharStreams;

public class E_XmlParse
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue nv) {
	    NodeValue result;
	    try {
	        result = resolve(nv);
	    } catch (Exception e) {
	    	throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
	    }

	    return result;
	}

	public static NodeValue resolve(NodeValue nv) throws Exception {
		RDFDatatypeXml dtype = (RDFDatatypeXml)TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);

		NodeValue result;
		try (InputStream in = E_UrlText.openInputStream(nv)) {
	    	if (in != null) {
	    		Node jenaNode = RDFDatatypeXml.parse(in, dtype);
	    		result = NodeValue.makeNode(jenaNode);
	    	} else {
	        	throw new ExprEvalException("Failed to obtain text from node " + nv);
	        }
		}

	    return result;
	}
}
