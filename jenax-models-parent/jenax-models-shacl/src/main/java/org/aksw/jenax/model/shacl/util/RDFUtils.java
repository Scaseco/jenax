package org.aksw.jenax.model.shacl.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.out.NodeFmtLib;

import com.google.common.html.HtmlEscapers;

public class RDFUtils {
    public Set<Property> properties(RDFNode rdfNode) {
        return rdfNode != null && rdfNode.isResource()
                ? new LinkedHashSet<>(rdfNode.asResource().listProperties().mapWith(Statement::getPredicate).toList())
                : Collections.emptySet();
    }

    public Set<RDFNode> objects(RDFNode rdfNode, Property p) {
        return rdfNode != null && rdfNode.isResource()
                ? new LinkedHashSet<>(rdfNode.asResource().listProperties(p).mapWith(Statement::getObject).toList())
                : Collections.emptySet();
    }

    public String toHtml(RDFNode rdfNode) {
        Node node = rdfNode.asNode();
        String result = toHtml(node);
        return result;
    }

    /**
     * Convert both arguments to strings, then return the result of arg1.contains(arg2).
     * Pebble seems to parse .contains() as its own operator rather than a method name, hence this helper.
     */
    public boolean strContains(Object str, Object substr) {
        boolean result;
        if (str == null) {
            result = false;
        } else {
            String strStr = Objects.toString(str);
            String substrStr = Objects.toString(substr);
            result = strStr.contains(substrStr);
        }
        return result;
    }

    public String toHtml(Node node) {
        String result;
        if (false) {
//            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder docBuilder;
//            try {
//                docBuilder = docFactory.newDocumentBuilder();
//            } catch (ParserConfigurationException e) {
//                throw new RuntimeException(e);
//            }
//
//            // root elements
//            Document doc = docBuilder.newDocument();
//            Element elt = doc.createElement("rdf-node");
//            elt.setAttribute("value", NodeFmtLib.strNT(node));
//            result = XmlUtils.toString(elt);
        } else {
            result = "<rdf-node value=\"" + HtmlEscapers.htmlEscaper().escape(NodeFmtLib.strNT(node)) + "\"></rdf-node>";
        }
        return result;
        //
    }
}
