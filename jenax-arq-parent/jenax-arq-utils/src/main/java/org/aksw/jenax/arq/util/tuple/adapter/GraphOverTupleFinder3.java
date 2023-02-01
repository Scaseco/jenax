package org.aksw.jenax.arq.util.tuple.adapter;

import org.aksw.commons.tuple.finder.TupleFinder3;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class GraphOverTupleFinder3
    extends GraphBase
{
    protected TupleFinder3<Triple, Node> tupleFinder;

    protected GraphOverTupleFinder3(TupleFinder3<Triple, Node> tupleFinder) {
        super();
        this.tupleFinder = tupleFinder;
    }

    public static Graph wrap(TupleFinder3<Triple, Node> tupleFinder) {
        return new GraphOverTupleFinder3(tupleFinder);
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
        return WrappedIterator.createNoRemove(
                tupleFinder.find(tp.getMatchSubject(), tp.getMatchPredicate(), tp.getObject()).iterator());
    }
}
