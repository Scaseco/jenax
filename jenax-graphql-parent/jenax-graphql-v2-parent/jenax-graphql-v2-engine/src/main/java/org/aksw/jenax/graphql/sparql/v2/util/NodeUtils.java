package org.aksw.jenax.graphql.sparql.v2.util;

import org.apache.jena.graph.Node;

public class NodeUtils {
    public static boolean isNullOrAny(Node node) {
        return node == null || Node.ANY.equals(node);
    }
}
