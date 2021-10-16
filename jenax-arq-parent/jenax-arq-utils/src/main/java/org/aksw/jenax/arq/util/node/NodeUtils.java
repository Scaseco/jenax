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


    /** Constants for use with {@link #getDatatypeIri(Node)}
        Conflates the term type into a datatype: Under this perspective, IRIs and
        blank nodes are simply literals with the rr:IRI or rr:BlankNode datatype.
        This approach simplifies analytics for e.g. application in schema mapping */
    public static final String R2RML_NS 					= "http://www.w3.org/ns/r2rml#";
    public static final String R2RML_IRI 					= R2RML_NS + "IRI";
    public static final String R2RML_BlankNode 				= R2RML_NS + "BlankNode";


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

    /**
     * Create a logical conjunction of two nodes:
     * - Node.ANY, null or a variable matches everything
     * - If any argument matches everything return the other argument (convert null to ANY)
     * - if both arguments are concrete nodes then return one if them if they are equal
     * - otherwise return null
     *
     */
    public static Node logicalAnd(Node pattern, Node b) {
        Node result = NodeUtils.isNullOrAny(pattern) || pattern.isVariable()
                ? nullToAny(b)
                : NodeUtils.isNullOrAny(b) || Objects.equals(pattern, b)
                    ? nullToAny(pattern)
                    : null;

        return result;
    }

    /**
     * Return the language of a node or null if the argument is not applicable
     *
     * @param node
     * @return
     */
    public static String getLang(Node node) {
        String result = node != null && node.isLiteral() ? node.getLiteralLanguage() : null;
        return result;
    }


    /**
     * Return a Node's datatype. Thereby, IRIs are returned as rr:IRI and BlankNodes as rr:BlankNode
     * Returns null for variables and unknown node types
     *
     * @param node
     */
    public static String getDatatypeIri(Node node) {
        String result;
        if (node == null) {
            result = null;
        } else if (node.isURI()) {
            result = R2RML_IRI;
        } else if (node.isBlank()) {
            result = R2RML_BlankNode;
        } else if (node.isLiteral()) {
            result = node.getLiteralDatatypeURI();
        } else {
            // Should not happen
            result = null;
        }

        return result;
    }

}
