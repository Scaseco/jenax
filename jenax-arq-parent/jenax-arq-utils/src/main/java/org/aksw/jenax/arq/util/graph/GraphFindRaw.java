package org.aksw.jenax.arq.util.graph;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * This interface is a workaround for graphs whose iterators may take very long
 * to produce the next item.
 * The find raw method may return candidate items which are post-filtered on the QueryIterator level.
 */
public interface GraphFindRaw {
    ExtendedIterator<Triple> findRaw(Triple triplePattern);
}
