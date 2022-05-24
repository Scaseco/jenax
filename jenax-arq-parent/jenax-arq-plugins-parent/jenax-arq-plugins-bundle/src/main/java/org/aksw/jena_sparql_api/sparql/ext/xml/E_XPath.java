package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathEvaluationResult.XPathResultType;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.apache.curator.shaded.com.google.common.collect.Iterators;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.rdfhdt.hdt.iterator.utils.Iter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

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

        // TODO If a node is of type string, we could still try to parse it as xml for convenience

        org.w3c.dom.Node xmlNode = JenaXmlUtils.extractXmlNode(nv);

    	if (xmlNode != null) {
	        if(query.isString()) {
	        	String queryStr = query.getString();

	            try {
	            	Iterator<org.apache.jena.graph.Node> nodes = JenaXmlUtils.evalXpath(xPathFactory, queryStr, xmlNode);
	            	org.apache.jena.graph.Node node = Iterators.getOnlyElement(nodes);
	            	if (node == null) {
	            		throw new ExprEvalException("XPath " + queryStr + " did not match any results");
	            	}

	            	result = NodeValue.makeNode(node);

	            } catch(Exception e) {
	                logger.warn(e.getLocalizedMessage());
	                result = null;
	            }
	        } else {
	        	result = null;
	        }
        } else {
            result = null;
        }

    	if (result == null) {
    		throw new ExprEvalException("xpath evaluation yeld null result");
    	}

        return result;
    }



}
