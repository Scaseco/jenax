package org.aksw.jena_sparql_api.sparql.ext.binding;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;

public class JenaBindingUtils {
    public static Binding requireBinding(NodeValue nv) {
        Binding elt = extractBindingOrNull(nv);
        if (elt == null) {
            NodeValue.raise(new ExprTypeException("Not a Binding"));
        }
        return elt;
    }

    public static Binding extractBindingOrNull(NodeValue nv) {
        Binding result = null;
        if (nv instanceof NodeValueBinding) {
            result = ((NodeValueBinding)nv).getBinding();
        } else {
            // Do we need this fallback?
            Node node = nv.getNode();
            if (node != null) {
                result = extractOrNull(node);
            }
        }
        return result;
    }

    public static Binding extractOrNull(Node node) {
        Binding result = null;
        if (node.isLiteral()) {
            Object value = node.getLiteralValue();
            if (value instanceof Binding) {
                result = (Binding)value;
            }
        }
        return result;
    }


}
