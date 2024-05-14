package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

/** Convert an XML Node to string */
public class E_XmlToText
    extends FunctionBase1
{
    public E_XmlToText() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        org.w3c.dom.Node xmlNode = JenaXmlUtils.extractXmlNode(nv);
        if (xmlNode != null) {
            String str = xmlNode.getTextContent();
            result = NodeValue.makeString(str);
        } else if (nv.isString()) {
            // could be that E_XPath was used to select an attribute, in which case we end up with a String here
            return nv;
        } else {
            throw new ExprEvalException("Argument is not an xml node");
        }
        return result;
    }
}

