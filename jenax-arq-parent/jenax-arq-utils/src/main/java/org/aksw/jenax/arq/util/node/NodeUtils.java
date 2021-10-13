package org.aksw.jenax.arq.util.node;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeUtils {

    /** Compare nodes via {@link NodeValue#compareAlways(NodeValue, NodeValue)} */
    public static int compareAlways(Node o1, Node o2) {
        int result;
        try {
            result = o1 == null
                ? o2 == null ? 0 : -1
                : o2 == null ? 1 : NodeValue.compareAlways(NodeValue.makeNode(o1), NodeValue.makeNode(o2));
        } catch (Exception e) {
            // RDF terms with mismatch in lexical value / datatype cause an exception
            result = NodeUtils.compareAlways(o1, o2);
        }
        return result;
    }


}
