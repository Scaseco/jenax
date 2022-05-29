package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class NodeValueXml extends NodeValue
{
    protected org.w3c.dom.Node xmlNode ;

    public NodeValueXml(org.w3c.dom.Node xmlNode)         { super() ; this.xmlNode = xmlNode ; }
    public NodeValueXml(org.w3c.dom.Node xmlNode, Node n) { super(n) ; this.xmlNode = xmlNode ; }

    public org.w3c.dom.Node getXmlNode() {
    	return xmlNode;
    }

    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteralByValue(xmlNode, RDFDatatypeXml.INSTANCE) ; }

    @Override
    public String asString() { return toString() ; }

    @Override
    public String toString()
    {
        // Preserve lexical form
        if ( getNode() != null ) return super.asString() ;  // str()
        return RDFDatatypeXml.INSTANCE.unparse(xmlNode);
    }

    @Override
    public void visit(NodeValueVisitor visitor) { }
}
