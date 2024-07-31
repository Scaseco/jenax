package org.aksw.jenax.graphql.sparql.v2.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

public class TripleUtils {
    public static Triple create(Node s, Node p, Node o, boolean isForward) {
        Triple result = isForward
            ? Triple.create(s, p, o)
            : Triple.create(o, p, s);
        return result;
    }

    public static Triple create(Node s, P_Path0 p, Node o) {
        Triple result = create(s, p.getNode(), o, p.isForward());
        return result;
    }
}
