package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.Comparator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.NodeCmp;

public class SparqlCxtNode
    implements SparqlCxt<Node>
{
    /**
     * Special constant that can be used in the graph component of tuples.
     * Avoids the need for an additional findNG method
     */
    public static final Node anyNamedGraph = NodeFactory.createURI("urn:x-arq:AnyNamedGraph");

    private static SparqlCxtNode INSTANCE = new SparqlCxtNode();

    public static SparqlCxtNode get() {
        return INSTANCE;
    }

    @Override
    public boolean isURI(Node node) {
        return node.isURI();
    }

    @Override
    public boolean isLiteral(Node node) {
        return node.isLiteral();
    }

    @Override
    public boolean isBlank(Node node) {
        return node.isBlank();
    }

    @Override
    public boolean isVar(Node node) {
        return node.isVariable();
    }

    @Override
    public boolean isNodeTriple(Node node) {
        return node.isNodeTriple();
    }

    @Override
    public boolean isAny(Node node) {
        return node == null || Node.ANY.equals(node);
    }

    @Override
    public boolean isConcrete(Node node) {
        return node.isURI();
    }

    @Override
    public Comparator<Node> comparator() {
        return NodeCmp::compareRDFTerms;
    }

    @Override
    public Node any() {
        return Node.ANY;
    }

    @Override
    public Node anyNamedGraph() {
        return anyNamedGraph;
    }

    @Override
    public Node unionGraph() {
        return Quad.unionGraph;
    }

    @Override
    public boolean isAnyNamedGraph(Node node) {
        return anyNamedGraph.equals(node);
    }

    @Override
    public boolean isUnionGraph(Node node) {
        return Quad.unionGraph.matches(node);
    }
}
