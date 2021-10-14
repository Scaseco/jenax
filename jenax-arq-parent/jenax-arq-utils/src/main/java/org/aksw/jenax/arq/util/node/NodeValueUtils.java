package org.aksw.jenax.arq.util.node;

import org.apache.jena.sparql.expr.NodeValue;

public class NodeValueUtils {
    /** Compare nodes via {@link NodeValue#compareAlways(NodeValue, NodeValue)} */
    public static int compareAlways(NodeValue o1, NodeValue o2) {
        int result;
        try {
            result = o1 == null
                ? o2 == null ? 0 : -1
                : o2 == null ? 1 : NodeValue.compareAlways(o1, o2);
        } catch (Exception e) {
            // RDF terms with mismatch in lexical value / datatype cause an exception
            result = org.apache.jena.sparql.util.NodeUtils.compareRDFTerms(o1.asNode(), o2.asNode());
        }
        return result;
    }

}
