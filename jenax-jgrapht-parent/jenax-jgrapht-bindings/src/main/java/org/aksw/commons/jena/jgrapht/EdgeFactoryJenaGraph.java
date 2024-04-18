package org.aksw.commons.jena.jgrapht;

import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
//import org.jgrapht.EdgeFactory;


public class EdgeFactoryJenaGraph
    implements
//    EdgeFactory<Node, Triple>,
    Supplier<Triple>
{
    protected Node predicate;

    public EdgeFactoryJenaGraph(Node predicate) {
        super();
        this.predicate = predicate;
    }

    //@Override
    public Triple createEdge(Node sourceVertex, Node targetVertex) {
        Triple result = Triple.create(sourceVertex, predicate, targetVertex);
        return result;
    }

    @Override
    public Triple get() {
        return Triple.create(Node.ANY, predicate, Node.ANY);
    }
}
