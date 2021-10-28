package org.aksw.jenax.arq.util.binding;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

public class RowSetUtils {
    public static Node getNextNode(RowSet rs, Var v) {
        Node result = null;

        if (rs.hasNext()) {
            Binding binding = rs.next();
            result = binding.get(v);
        }
        return result;
    }

    public static Optional<Node> tryGetNextNode(RowSet rs, Var v) {
        Node node = getNextNode(rs, v);
        Optional<Node> result = Optional.ofNullable(node);
        return result;
    }

}
