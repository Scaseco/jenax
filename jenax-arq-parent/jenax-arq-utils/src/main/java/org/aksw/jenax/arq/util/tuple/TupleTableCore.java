package org.aksw.jenax.arq.util.tuple;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.jenax.arq.util.tuple.query.TupleQuery;
import org.aksw.jenax.arq.util.tuple.query.TupleQueryImpl;
import org.aksw.jenax.arq.util.tuple.query.TupleQuerySupport;
import org.aksw.jenax.arq.util.tuple.resultset.ResultStreamer;
import org.aksw.jenax.arq.util.tuple.resultset.ResultStreamerFromTuple;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;

/**
 * Base interface for tuples i.e. triples and quads.
 * Evolving.
 * Deliberately analogous to {@link org.apache.jena.sparql.core.mem.TupleTable} to enable potential
 * future consolidation
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <T> The type of the tuples to hold
 */
public interface TupleTableCore<T, X>
    extends TupleQuerySupport<T, X>
{
    /**
     * Clear all entries
     *
     * Optional operation. May fail with {@link UnsupportedOperationException} for e.g. views.
     */
    void clear();

    void add(T tuple);
    void delete(T tuple);
    boolean contains(T tuple);

    // <T> Stream<TupleType> find(T lookup, TupleAccessor<? super T, ? extends ComponentType> accessor);

    /**
     * The basic find method that yields all tuples whose components equal
     * the corresponding non-null components in pattern.
     *
     * Be aware that the contract for this method is that only null values in the pattern
     * correspond to 'match any'
     *
     * Specifically, don't use Node.ANY here or it may result in an attempt to match tuples
     * with this value exactly. On the tuple-level we don't know or care about such domain conventions.
     *
     *
     * @param tupleTypes
     * @return
     */
    default Stream<T> findTuples(List<X> pattern) {
        return findTuples(pattern, List::get);
    }

    <D> Stream<T> findTuples(D domainTyple, TupleAccessor<? super D, ? extends X> accessor);

    Stream<T> findTuples();

    /**
     *
     *
     * @return
     */
    @Override
    default ResultStreamer<T, X, Tuple<X>> find(TupleQuery<X> tupleQuery) {
        List<X> pattern = tupleQuery.getPattern();

        // The projector is the function that projects a domain object into an appropriate tuple w.r.t.
        // the given projection indices
        int[] project = tupleQuery.getProject();
        Function<T, Tuple<X>> projector = TupleOps.createProjector(project, getTupleAccessor());

        Supplier<Stream<Tuple<X>>> tupleStreamSupplier = () -> {
            Stream<T> domainStream = findTuples(pattern);
            Stream<Tuple<X>> tupleStream = domainStream.map(projector::apply);

            if (tupleQuery.isDistinct()) {
                tupleStream = tupleStream.distinct();
            }
            return tupleStream;
        };

        return new ResultStreamerFromTuple<>(getDimension(), tupleStreamSupplier, getTupleAccessor());
    }

    /** Convenience fluent API that is tied to this tuple table */
//    default TupleFinder<T, T, X> newFinder() {
//        return TupleFinderImpl.create(this);
//    }


    /**
     * The number of tuples in the table.
     * If not indexed then this value is used as the estimate of lookups
     * Therefore the size should be cached
     *
     * @return
     */
    default long size() {
        return find(new TupleQueryImpl<>(getDimension())).streamAsTuple().count();
    }

    /**
     * By default there is just the root index node which claims that
     * any lookup performs a full scan on the data
     *
     * @return
     */

    /**
     * The number of components / columns
     *
     * @return
     */
    default int getDimension() {
        return getTupleAccessor().getDimension();
    }


    TupleBridge<T, X> getTupleAccessor();



    // Does not belong here because tuples are generic and ANY is a domain value
    public static Node nullToAny(Node n) {
        return n == null ? Node.ANY : n;
    }

    public static Node anyToNull(Node n) {
        return Node.ANY.equals(n) ? null : n;
    }

}