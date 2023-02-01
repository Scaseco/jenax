package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.tuple.finder.TupleFinder;
import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperFindBase;
import org.aksw.jenax.arq.util.tuple.IterUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.Quad;

/**
 * A Dataset wrapper that delegates all find-related (including contains) invocations to
 * a tuple finder.
 * All other methods (e.g. add, remove, begin, ...) delegate to the wrapped dataset.
 *
 * @author raven
 *
 * @param <D>
 * @param <C>
 */
public class DatasetGraphOverTupleFinder<D, C>
    extends DatasetGraphWrapperFindBase
    implements DatasetGraphWrapperView
{
    protected TupleFinder<Quad, Node> tupleFinder;

    protected DatasetGraphOverTupleFinder(DatasetGraph dsg, TupleFinder<Quad, Node> tupleFinder) {
        super(dsg);
        this.tupleFinder = Objects.requireNonNull(tupleFinder);
    }

    public static DatasetGraph wrap(DatasetGraph dsg, TupleFinder<Quad, Node> tupleFinder) {
        return new DatasetGraphOverTupleFinder<>(dsg, tupleFinder);
    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node g, Node s, Node p, Node o) {
        Node gg = ng && (Node.ANY.equals(g) || g == null) ? SparqlCxtNode.anyNamedGraph : g;
        Stream<Quad> stream = tupleFinder.find(gg, s, p, o);
        return IterUtils.iter(stream);
    }
}
