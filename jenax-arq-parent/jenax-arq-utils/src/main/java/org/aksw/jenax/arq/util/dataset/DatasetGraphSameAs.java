package org.aksw.jenax.arq.util.dataset;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiPredicate;

import org.aksw.jenax.arq.dataset.cache.CachePatterns;
import org.aksw.jenax.arq.dataset.cache.DatasetGraphCache;
import org.aksw.jenax.arq.util.tuple.adapter.DatasetGraphOverTupleFinder;
import org.aksw.jenax.arq.util.tuple.adapter.SparqlCxtNode;
import org.aksw.jenax.arq.util.tuple.adapter.TupleFinderOverDatasetGraph;
import org.aksw.jenax.arq.util.tuple.impl.TupleFinderSameAs;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.vocabulary.OWL;

/** Static methods to wrap a DatasetGraph with sameAs inferences */
public class DatasetGraphSameAs {
    public static DatasetGraph wrap(DatasetGraph base) {
        return wrap(base, OWL.sameAs.asNode());
    }

    public static DatasetGraph wrap(DatasetGraph base, Node sameAsPredicate) {
        return wrap(base, Collections.singleton(sameAsPredicate), false);
    }

    public static DatasetGraph wrap(DatasetGraph base, Set<Node> sameAsPredicates) {
        return wrap(base, sameAsPredicates, false);
    }

    public static DatasetGraph wrap(DatasetGraph base, Set<Node> sameAsPredicates, boolean allowDuplicates) {
        // Apply DatasetGraph -> TupleFinder -> DatasetGraph wrapping
        return DatasetGraphOverTupleFinder.wrap(base, TupleFinderSameAs.wrap(
                TupleFinderOverDatasetGraph.wrap(base, SparqlCxtNode.get()::isAnyNamedGraph),
                SparqlCxtNode.get(), sameAsPredicates, allowDuplicates));
    }


    public static DatasetGraph wrapWithTable(DatasetGraph base, Node sameAsPredicate) {
        return wrapWithTable(base, Collections.singleton(sameAsPredicate), false);
    }

    public static DatasetGraph wrapWithTable(DatasetGraph base, Node sameAsPredicate, boolean allowDuplicates) {
        return wrapWithTable(base, Collections.singleton(sameAsPredicate), allowDuplicates);
    }

    /**
     * This method sets up a tabling DatasetGraphWrapper for the patterns
     * (IN, IN, sameAs, ANY) and (IN, ANY, sameAs, IN).
     *
     * The core inferencer TupleFinderSameAs consults the cache before attempting to compute transitive closures for resources.
     */
    public static DatasetGraph wrapWithTable(DatasetGraph base, Set<Node> sameAsPredicates, boolean allowDuplicates) {
        DatasetGraphCache cache = DatasetGraphCache.table(base, CachePatterns.forNeigborsByPredicates(sameAsPredicates));

        BiPredicate<Node, Node> mayHaveSameAsLinks = (g, n) -> {
            boolean r = false;
            for (Node p : sameAsPredicates) {
                boolean mayHaveOutLinks = cache.mayContainQuad(g, n, p, Node.ANY);
                boolean mayHaveInLinks = cache.mayContainQuad(g, Node.ANY, p, n);
                r = mayHaveInLinks || mayHaveOutLinks;
                if (r) {
                    break;
                }
            }
            // System.out.println("mayHaveSameAsLinks(" + g + ", " + n + "): " + r);
            return r;
        };

        // Apply DatasetGraph -> TupleFinder -> DatasetGraph wrapping
        return DatasetGraphOverTupleFinder.wrap(base, TupleFinderSameAs.wrap(
                TupleFinderOverDatasetGraph.wrap(cache, SparqlCxtNode.get()::isAnyNamedGraph),
                SparqlCxtNode.get(), sameAsPredicates, allowDuplicates, mayHaveSameAsLinks));
    }

}
