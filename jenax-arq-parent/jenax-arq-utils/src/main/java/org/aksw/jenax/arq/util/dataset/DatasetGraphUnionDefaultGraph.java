package org.aksw.jenax.arq.util.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * A dataset wrapper that redirects find requests on the default graph to the union graph.
 * All named graphs of quads are remapped to the default graph to make them appear as real default graph quads.
 * Usually, a lookup with Quad.unionGraph retains the named graphs.
 *
 *
 * Mainly intended for wrapping TDB2 datasets which have the defaultUnionGraph context symbol set to true
 * such that {@code find(Quad.defaultGraph, ...)} is transparently redirected to the union graph.
 */
public class DatasetGraphUnionDefaultGraph
    extends DatasetGraphWrapperFindBase
    implements DatasetGraphWrapperView
{
    public DatasetGraphUnionDefaultGraph(DatasetGraph dsg) {
        super(dsg);
    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node g, Node s, Node p, Node o) {
        // All find requests affect the named graphs
        Iterator<Quad> result;
        if (Quad.isDefaultGraph(g)) { // || Quad.isUnionGraph(g)) {
            result = findInUdf(s, p, o);
        } else {
            // A response for ng=false and g=Node.ANY must include the union default graph
            Iter<Boolean> emitUdf = Node.ANY.equals(g) && !ng
                    ? Iter.of(true, false)
                    : Iter.of(false);

            result = emitUdf.flatMap(udf -> udf
                    ? findInUdf(s, p, o)
                    : getR().findNG(g, s, p, o));
        }
        return result;
    }

    private Iterator<Quad> findInUdf(Node s, Node p, Node o) {
        return Iter.iter(getR().findNG(Quad.unionGraph, s, p, o))
                .map(q -> Quad.defaultGraphIRI.equals(q.getGraph())
                        ? q
                        : Quad.create(Quad.defaultGraphIRI, q.getSubject(), q.getPredicate(), q.getObject()));
    }

    /** Wrap a given dataset if it is not already wrapped by this class */
    public static DatasetGraph wrap(DatasetGraph dsg) {
        DatasetGraph result = dsg instanceof DatasetGraphUnionDefaultGraph ? dsg : new DatasetGraphUnionDefaultGraph(dsg);
        return result;
    }
    /** Wrap a given dataset <b>only if it is known to be in union default graph mode</b> and not already wrapped by this class */
    public static DatasetGraph wrapIfNeeded(DatasetGraph dsg) {
        DatasetGraph result = !(dsg instanceof DatasetGraphUnionDefaultGraph) && isKnownUnionDefaultGraphMode(dsg)
                ? new DatasetGraphUnionDefaultGraph(dsg)
                : dsg;
        return result;
    }

    /*
     * Infrastructure for determining whether a dataset graph is in union graph mode
     */

    private static Set<Predicate<DatasetGraph>> knownUnionDefaultGraphCheckers;

    /**
     * The set of predicates returned by this method is synchronized. Plugins may modify the elements.
     * Modifications only affect future {@link DatasetGraphUnionDefaultGraph} instances.
     */
    public static Set<Predicate<DatasetGraph>> getUnionDefaultGraphCheckers() {
        if (knownUnionDefaultGraphCheckers == null) {
            synchronized (DatasetGraphUnionDefaultGraph.class) {
                if (knownUnionDefaultGraphCheckers == null) {
                    knownUnionDefaultGraphCheckers = Collections.synchronizedSet(new LinkedHashSet<>());
                    knownUnionDefaultGraphCheckers.add(dsg -> isTdbUnionDefaultGraph(dsg.getContext()));
                }
            }
        }
        return knownUnionDefaultGraphCheckers;
    }

    private static final Symbol tdbSymbol1 = Symbol.create("http://jena.hpl.hp.com/TDB#unionDefaultGraph");
    private static final Symbol tdbSymbol2 = Symbol.create("http://jena.apache.org/TDB#unionDefaultGraph");

    public static boolean isTdbUnionDefaultGraph(Context cxt) {
        boolean result = cxt != null && (cxt.isTrue(tdbSymbol2) || cxt.isTrue(tdbSymbol1));
        return result;
    }

    public static boolean isKnownUnionDefaultGraphMode(DatasetGraph dsg) {
        boolean result = false;
        for (Predicate<DatasetGraph> predicate : getUnionDefaultGraphCheckers()) {
            result = predicate.test(dsg);
            if (result) {
                break;
            }
        }
        return result;
    }
}
