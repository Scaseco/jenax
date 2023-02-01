package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.stream.Stream;

import org.aksw.commons.tuple.bridge.TupleBridge3;
import org.aksw.commons.tuple.finder.TupleFinder3;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TupleFinder3OverGraph
    implements TupleFinder3<Triple, Node>
{
    protected Graph base;

    public TupleFinder3OverGraph(Graph base) {
        super();
        this.base = base;
    }

    public static TupleFinder3<Triple, Node> wrap(Graph graph) {
        return new TupleFinder3OverGraph(graph);
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return base.stream(s, p, o);
    }

    @Override
    public TupleBridge3<Triple, Node> getTupleBridge() {
        return TupleBridgeTriple.get();
    }
}
