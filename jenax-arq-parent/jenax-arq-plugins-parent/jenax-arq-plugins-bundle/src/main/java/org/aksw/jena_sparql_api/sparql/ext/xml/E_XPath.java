package org.aksw.jena_sparql_api.sparql.ext.xml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathEvaluationResult.XPathResultType;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Use the property function ?s xml:unnest (xpathexpr ?o) for dealing with (/iterating) NodeList
 * Use the function xml:path(?doc, pathexpr) to extract individual values
 * 
 * @author raven Mar 2, 2018
 *
 */
public class E_XPath
	extends FunctionBase2
{	
	private static final Logger logger = LoggerFactory.getLogger(E_XPath.class);

	
	protected XPath xPath;

	public E_XPath() {
		 this(XPathFactory.newInstance().newXPath());
	}
	
	public E_XPath(XPath xPath) {
		 this.xPath = xPath;
	}
	

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {
		RDFDatatype xmlDatatype = TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);

        NodeValue result;

        // TODO If a node is of type string, we could still try to parse it as xml for convenience
        
        Object obj = nv.asNode().getLiteralValue();
    	if(obj instanceof Node) {
	    	Node xml = (Node)obj;
	    	// If 'xml' is a Document with a single node, use the node as the context for the xpath evaluation
	    	// Reason: Nodes matched by xml:unnest will be wrapped into new (invisible) XML documents
	    	// We would expect to be able to run xpath expressions directly on the
	    	// result nodes of the unnesting - without having to consider the invisible document root node
			NamespaceResolver namespaceResolver = new NamespaceResolver(xml);
			if(xml instanceof Document && xml.getChildNodes().getLength() == 1) {
	    		xml = xml.getFirstChild();
	    	}
	    	
	    	//System.out.println(XmlUtils.toString(xml));

	        if(query.isString() && xml != null) {
	        	String queryStr = query.getString();	        	
	        		        	
	            try {
					xPath.setNamespaceContext(namespaceResolver);
	            	XPathExpression expr = xPath.compile(queryStr);
	            	// Object tmp = expr.evaluate(xml, XPathConstants.STRING);

	            	XPathEvaluationResult<?> er = expr.evaluateExpression(xml);
	            	org.apache.jena.graph.Node node = toNode(er, xmlDatatype);
	            	
	            	if (node == null) {
	            		throw new ExprEvalException("XPath " + queryStr + " did not match any results");
	            	}
	            	
	            	result = NodeValue.makeNode(node);
	            	
	            	
//	            	if(tmp instanceof NodeList) {
//	            		NodeList nodes = (NodeList)tmp;
//	            		for(int i = 0; i < nodes.getLength(); ++i) {
//	            			Node node = nodes.item(i);
//	            			//System.out.println("" + node);
//	            		}
//	            	}
	            	
		        	//Object tmp = xPath.evaluate(queryStr, xml, XPathConstants.STRING);
		        	// FIXME Hack
		        	// result = NodeValue.makeString("" + tmp);
	            } catch(Exception e) {
	                logger.warn(e.getLocalizedMessage());
	                result = null;
	            }
	        } else {
	        	result = null; //Expr.NONE.getConstant();
	        }
        } else {
            result = null; //Expr.NONE.getConstant();
        }
    	//System.out.println(result);

    	if (result == null) {
    		throw new ExprEvalException("xpath evaluation yeld null result");
    	}
    	
        return result;
    }
    
    
    
    public static org.apache.jena.graph.Node toNode(XPathEvaluationResult<?> er, RDFDatatype xmlDatatype) {
    	org.apache.jena.graph.Node result = null;
    	XPathResultType type = er.type();
    	Object value = er.value();
    	Class<?> cls = value.getClass();
    	
    	TypeMapper tm = TypeMapper.getInstance();
    	
    	switch (type) {
    	case BOOLEAN:
    	case NUMBER:
    	case STRING:
        	RDFDatatype dtype = tm.getTypeByClass(cls);
        	result = NodeFactory.createLiteralByValue(value, dtype);
    		break;
    	case NODE:
    		throw new IllegalStateException("Not implemented yet");
    	case NODESET:
    		XPathNodes nodes = (XPathNodes)value;
    		for (Node node : nodes) {
    			if (result != null) {
    				throw new IllegalArgumentException("Node set must contain at most one value");
    			}
    			
    			if (node instanceof Attr) {
    				Attr attr = (Attr)node;
    				result = NodeFactory.createLiteral(attr.getValue());
    			} else {    			
    				result = NodeFactory.createLiteralByValue(node, xmlDatatype);
    			}
    		}
    		break;
    	case ANY:
    	default:
    		throw new IllegalStateException("Should never come here: Result type was: " + type);
    	}
    	
    	return result;
    }

}
