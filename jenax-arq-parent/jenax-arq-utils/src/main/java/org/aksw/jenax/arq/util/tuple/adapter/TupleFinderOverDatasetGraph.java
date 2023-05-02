package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.commons.tuple.bridge.TupleBridge4;
import org.aksw.commons.tuple.finder.TupleFinder4;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/**
 * A TupleTable view over a DatasetGraph.
 */
public class TupleFinderOverDatasetGraph
    implements TupleFinder4<Quad, Node>
{
    protected DatasetGraph delegate;

    /**
     * This predicate is used to decide whether to delegate a find()
     * request on this class to the underlying DatasetGraph's find()
     * or findNG() method.
     */
    protected Predicate<Node> findInAnyNG;

    protected TupleFinderOverDatasetGraph(DatasetGraph delegate, Predicate<Node> findInAnyNG) {
        super();
        this.delegate = delegate;
        this.findInAnyNG = findInAnyNG;
    }

    public static TupleFinder4<Quad, Node> wrap(DatasetGraph dsg, Predicate<Node> useFindNG) {
        return new TupleFinderOverDatasetGraph(dsg, useFindNG);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        boolean ng = findInAnyNG.test(g);
        Node gg = ng ? Node.ANY : g;

        Iterator<Quad> it = ng
                ? delegate.findNG(gg, s, p, o)
                : delegate.find(g, s, p, o);

        return Iter.asStream(it);
    }

    @Override
    public TupleBridge4<Quad, Node> getTupleBridge() {
        return TupleBridgeQuad.get();
    }
}
