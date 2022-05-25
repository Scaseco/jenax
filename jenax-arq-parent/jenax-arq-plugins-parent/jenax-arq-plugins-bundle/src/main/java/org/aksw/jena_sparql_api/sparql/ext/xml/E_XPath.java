package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected XPathFactory xPathFactory;

	public E_XPath() {
		this(XPathFactory.newInstance());
	}

	public E_XPath(XPathFactory xPathFactory) {
		 this.xPathFactory = xPathFactory;
	}

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {
        NodeValue result;

        org.w3c.dom.Node xmlNode = JenaXmlUtils.extractXmlNode(nv);

    	if (xmlNode != null) {
	        if(query.isString()) {
	        	String queryStr = query.getString();

            	Iterator<org.apache.jena.graph.Node> nodes;
				try {
					nodes = JenaXmlUtils.evalXPath(xPathFactory, queryStr, xmlNode);
				} catch (XPathExpressionException e) {
					logger.warn("XPath evaluation failed", e);
					throw new ExprEvalException("XPath evaluation failed");
				}

            	// Ensure that hasNext() is called on the iterator due to misbehavior:
            	// Guava's getOnlyElement() relies on a NoSuchElement exception when calling next() on an empty iterator
            	// But at least one jena iterator returns null instead
            	if (nodes.hasNext()) {
            		org.apache.jena.graph.Node node = nodes.next();

            		if (nodes.hasNext()) {
            			throw new ExprEvalException("XPath " + queryStr + " evaluated to multiple results");
            		}

	            	result = NodeValue.makeNode(node);
            	} else {
            		throw new ExprEvalException("XPath " + queryStr + " did not match any results");
            	}

	        } else {
        		throw new ExprEvalException("XPath query argument is not a string: " + query);
	        }
        } else {
    		throw new ExprEvalException("XPath xml node argument is not an xml node ");
        }

    	if (result == null) {
    		throw new ExprEvalException("xpath evaluation yeld null result");
    	}

        return result;
    }
}

