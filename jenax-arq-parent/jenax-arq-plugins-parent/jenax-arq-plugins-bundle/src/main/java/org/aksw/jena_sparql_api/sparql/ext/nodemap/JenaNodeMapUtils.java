package org.aksw.jena_sparql_api.sparql.ext.nodemap;

import org.aksw.jenax.arq.util.node.NodeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;

public class JenaNodeMapUtils {
    public static NodeMap requireNodeMap(NodeValue nv) {
        NodeMap elt = extractNodeMapOrNull(nv);
        if (elt == null) {
            NodeValue.raise(new ExprTypeException("Not a Binding"));
        }
        return elt;
    }

    public static NodeMap extractNodeMapOrNull(NodeValue nv) {
        NodeMap result = null;
        if (nv instanceof NodeValueNodeMap) {
            result = ((NodeValueNodeMap)nv).getBinding();
        } else {
            // Do we need this fallback?
            Node node = nv.getNode();
            if (node != null) {
                result = extractOrNull(node);
            }
        }
        return result;
    }

    public static NodeMap extractOrNull(Node node) {
        NodeMap result = null;
        if (node.isLiteral()) {
            Object value = node.getLiteralValue();
            if (value instanceof NodeMap) {
                result = (NodeMap)value;
            }
        }
        return result;
    }
}
