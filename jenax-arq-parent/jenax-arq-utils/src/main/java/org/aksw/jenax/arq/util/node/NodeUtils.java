package org.aksw.jenax.arq.util.node;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Streams;

public class NodeUtils {

    /** Placeholder constants to denote a 'null' node */
    public static final String nullUri = "http://null.null/null";
    public static final Node nullUriNode = NodeFactory.createURI(nullUri);


    /** Compare nodes via {@link NodeValue#compareAlways(NodeValue, NodeValue)} */
    public static int compareAlways(Node o1, Node o2) {
        int result;
        try {
            result = o1 == null
                ? o2 == null ? 0 : -1
                : o2 == null ? 1 : NodeValue.compareAlways(NodeValue.makeNode(o1), NodeValue.makeNode(o2));
        } catch (Exception e) {
            // RDF terms with mismatch in lexical value / datatype cause an exception
            result = org.apache.jena.sparql.util.NodeUtils.compareRDFTerms(o1, o2);
        }
        return result;
    }


    /** Filter an iterable of nodes to the set of contained bnodes */
    public static Set<Node> getBnodesMentioned(Iterable<Node> nodes) {
        Set<Node> result = Streams.stream(nodes)
                .filter(Objects::nonNull)
                .filter(Node::isBlank)
                .collect(Collectors.toSet());

        return result;
    }

    /** Filter an iterable of nodes to the set of contained variables */
    public static Set<Var> getVarsMentioned(Iterable<Node> nodes)
    {
        Set<Var> result = Streams.stream(nodes)
                .filter(Objects::nonNull)
                .filter(Node::isVariable)
                .map(node -> (Var)node)
                .collect(Collectors.toSet());

        return result;
    }



    public static boolean isNullOrAny(Node node) {
        return node == null || Node.ANY.equals(node);
    }

    /** This method is unfortunately private in {@link Triple} at least in jena 3.16 */
    public static Node nullToAny(Node n) {
        return n == null ? Node.ANY : n;
    }


}
