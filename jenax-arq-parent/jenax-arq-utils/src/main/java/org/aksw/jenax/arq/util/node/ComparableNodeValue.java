package org.aksw.jenax.arq.util.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeUtils;

/**
 * A wrapper that makes NodeValues comparable by their value.
 * For example, "5"^^xsd:int equals "5"^^xsd:double.
 *
 * Allows for use in guava's RangeSet.
 *
 * @author raven
 *
 */
public class ComparableNodeValue
    implements Comparable<ComparableNodeValue>, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Node node;
    protected transient NodeValue nodeValue;


    /** For serialization */
    ComparableNodeValue() {
    }

    protected ComparableNodeValue(Node node, NodeValue nodeValue) {
        super();
        this.node = node;
        this.nodeValue = nodeValue;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ComparableNodeValue obj = (ComparableNodeValue) ois.readObject();
        obj.nodeValue = NodeValue.makeNode(obj.node);
    }

    public Node getNode() {
        return node;
    }

    public NodeValue getNodeValue() {
        return nodeValue;
    }

    public static ComparableNodeValue wrap(Node node) {
        return new ComparableNodeValue(node, NodeValue.makeNode(node));
    }

    public static ComparableNodeValue wrap(NodeValue nodeValue) {
        return new ComparableNodeValue(nodeValue.asNode(), nodeValue);
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
    public int compareTo(ComparableNodeValue that) {
        int result;
        try {
            result = NodeValue.compare(nodeValue, that.nodeValue);
        } catch (ExprNotComparableException e) {
            result = NodeUtils.compareRDFTerms(this.node, that.node);
        }

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
        ComparableNodeValue other = (ComparableNodeValue) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        return true;
    }
}
