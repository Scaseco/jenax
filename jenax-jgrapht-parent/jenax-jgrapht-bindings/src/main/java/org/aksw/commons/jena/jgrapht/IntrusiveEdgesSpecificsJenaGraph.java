package org.aksw.commons.jena.jgrapht;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;

public class IntrusiveEdgesSpecificsJenaGraph
    implements IntrusiveEdgesSpecifics<Node, Triple>
{
    /**
     *
     */
    private static final long serialVersionUID = 6058434931932958438L;

    protected Graph graph;
    protected Node confinementPredicate;

    public IntrusiveEdgesSpecificsJenaGraph(Graph graph, Node confinementPredicate) {
        super();
        this.graph = graph;
        this.confinementPredicate = confinementPredicate;
    }

    @Override
    public Node getEdgeSource(Triple e) {
        return e.getSubject();
    }

    @Override
    public Node getEdgeTarget(Triple e) {
        return e.getObject();
    }

    @Override
    public boolean add(Triple e, Node sourceVertex, Node targetVertex) {
        Triple stmt = Triple.create(sourceVertex, confinementPredicate, targetVertex);
        boolean tmp = graph.contains(stmt);
        if(!tmp) {
            graph.add(stmt);
        }
        boolean result = !tmp;
        return result;
    }

    @Override
    public boolean containsEdge(Triple e) {
        boolean result = graph.contains(e);
        return result;
    }

    @Override
    public Set<Triple> getEdgeSet() {
        return new SetOfTriplesFromGraph(graph, confinementPredicate);
    }

    @Override
    public void remove(Triple e) {
        graph.remove(e.getSubject(), e.getPredicate(), e.getObject());
    }

    @Override
    public double getEdgeWeight(Triple e) {
        return 1.0;
    }

    @Override
    public void setEdgeWeight(Triple e, double weight) {
        if(weight != 1.0) {
            throw new UnsupportedOperationException();
        }
    }

}
