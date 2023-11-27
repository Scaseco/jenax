package org.aksw.jenax.arq.util.node;

import java.io.Serializable;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeCmp;

/**
 * A wrapper that makes Nodes comparable by rdf term equivalence.
 * For example, "5"^^xsd:int DOES NOT equal "5"^^xsd:double.
 *
 * Allows for use in guava's RangeSet.
 *
 * @author raven
 *
 */
public class ComparableNode
    implements Comparable<ComparableNode>, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Node node;

    protected ComparableNode(Node node) {
        super();
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public static ComparableNode wrap(Node node) {
        return new ComparableNode(node);
    }


    /**
     * Compare strictly by value if possible.
     * Comparing 5 (int) to to 5.0 (double) can thus yield 0.
     *
     * In contrast, {@link NodeValue#compareAlways(NodeValue, NodeValue)}
     * discriminates equal values further by the rdf term.
     *
     * fallback to lexical comparison
     */
    @Override
    public int compareTo(ComparableNode that) {
        int result = NodeCmp.compareRDFTerms(node, that.node);
        return result;
    }

    @Override
    public String toString() {
        return node.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComparableNode other = (ComparableNode) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        return true;
    }
}
