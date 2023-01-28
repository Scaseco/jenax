package org.aksw.jenax.arq.sameas.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperFindBase;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.vocabulary.OWL;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.graph.Traverser;


/**
 * A stateless DatasetGraph wrapper whose find() methods apply same-as inferences for the configured
 * predicates.
 */
public class DatasetGraphSameAs
    extends DatasetGraphWrapperFindBase
    implements DatasetGraphWrapperView
{
    protected boolean logCacheStats = false;

    /** Allowing duplicates disables certain checks which may increase performance */
    protected boolean allowDuplicates;

    protected Set<Node> sameAsPredicates;

    public static DatasetGraph wrap(DatasetGraph base) {
        return wrap(base, OWL.sameAs.asNode());
    }

    public static DatasetGraph wrap(DatasetGraph base, Node sameAsPredicate) {
        return new DatasetGraphSameAs(base, Collections.singleton(sameAsPredicate), false);
    }

    public static DatasetGraph wrap(DatasetGraph base, Set<Node> sameAsPredicates) {
        return new DatasetGraphSameAs(base, sameAsPredicates, false);
    }

    public static DatasetGraph wrap(DatasetGraph base, Set<Node> sameAsPredicates, boolean allowDuplicates) {
        return new DatasetGraphSameAs(base, sameAsPredicates, allowDuplicates);
    }

    protected DatasetGraphSameAs(DatasetGraph base, Set<Node> sameAsPredicates, boolean allowDuplicates) {
        super(base);
        this.sameAsPredicates = sameAsPredicates;
        this.allowDuplicates = allowDuplicates;
    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node mg, Node ms, Node mp, Node mo) {
        // These caches are local to the find() call in order to avoid complex synchronization w.r.t. updates
        // Cache sizes could be made configurable.
        Cache<Entry<Node, Node>, List<Node>> sameAsCache =
                CacheUtils.recordStats(CacheBuilder.newBuilder(), logCacheStats)
                .concurrencyLevel(1).maximumSize(10).build();

        // This cache on our load did not generate sufficient hits - therefore disabled
        Cache<Quad, Quad> leastInferredToPhysicalQuadCache = null; // CacheBuilder.newBuilder().recordStats().concurrencyLevel(1).maximumSize(100).build();

        // If s or o is concrete then resolve their sameAs closure before the lookup
        // Otherwise, resolve the sameAs closures of that component based on the obtained triple's concrete values

        // Note: resolveSameAs only actually resolves the given start node if both it and the graph are concrete;
        // Otherwise the start node is returned as-is.

        // Set up the tuple stream (quads)
        List<Node> initialSubjects = resolveSameAsSortedCached(mg, ms, sameAsCache);
        List<Node> initialObjects = resolveSameAsSortedCached(mg, mo, sameAsCache);

        Iterator<Quad> result =
            Iter.iter(initialSubjects).flatMap(s ->
                Iter.iter(initialObjects).flatMap(o ->
                    Iter.iter(delegateFind(ng, mg, s, mp, o))
                        .flatMap(t -> streamInferencesOnLeastQuad(t, ms, mo, sameAsCache, leastInferredToPhysicalQuadCache))
                ));

        // Log cache stats once the iterator is closed
        if (logCacheStats) {
            result = Iter.onClose(result, () -> {
                System.out.println("SameAsCache: "+ CacheUtils.stats(sameAsCache));
                System.out.println("LeastQuadCache: "+ CacheUtils.stats(leastInferredToPhysicalQuadCache));
            });
        }

        return result;
    }

    private Iterator<Quad> streamInferencesOnLeastQuad(Quad quad, Node ms, Node mo, Cache<Entry<Node, Node>, List<Node>> sameAsCache, Cache<Quad, Quad> leastQuadCache) {
        Iterator<Quad> result;
        Node g = quad.getGraph();
        Node p = quad.getPredicate();
        List<Node> sortedSubjects = resolveSameAsSortedCached(g, quad.getSubject(), sameAsCache);
        List<Node> sortedObjects = resolveSameAsSortedCached(g, quad.getObject(), sameAsCache);

        // The quad and the same-as closures of the subjects and objects
        // are the basis to create the set of inferred quads
        // However, inferences should only be emitted on the 'least' quad that is contained in the graph
        // For performance it is crucial to minimize lookups via contains()

        if (sortedSubjects.size() == 1 && sortedObjects.size() == 1) {
            // Don't call contains if there is there are no same-as'd alternatives of it
            result = Iter.of(quad);
        } else {
            Node leastS = sortedSubjects.get(0);
            Node leastO = sortedObjects.get(0);
            Quad leastInferrableQuad = Quad.create(g, leastS, p, leastO);
            Quad leastPhysicalQuad;

            boolean isLeastQuad;
            if (!allowDuplicates) {
                leastPhysicalQuad = CacheUtils.get(leastQuadCache, leastInferrableQuad, () -> computeLeastPhysicalQuad(leastInferrableQuad, sortedSubjects, sortedObjects));
                isLeastQuad = quad.equals(leastPhysicalQuad);
            } else {
                isLeastQuad = true;
            }
            // If there were concrete subjects/objects for matching then restrict the cross-join
            // only to those
            List<Node> ss = ms.isConcrete() ? Collections.singletonList(ms) : sortedSubjects;
            List<Node> oo = mo.isConcrete() ? Collections.singletonList(mo) : sortedObjects;

            // System.out.println(quad + (isLeastQuad ? " is least quad " : " hasLeastQuad: " + leastQuad));
            result = isLeastQuad
                    ? Iter.iter(ss).flatMap(s -> Iter.iter(oo).map(o -> Quad.create(g, s, p, o)))
                    : Iter.empty();

//            if (!isLeastQuad) {
//                System.out.println("IsLeast: " + isLeastQuad + " " + quad);
//            }
        }
        return result;
    }

    private Quad computeLeastPhysicalQuad(Quad quad, List<Node> sortedSubjects, List<Node> sortedObjects) {
        // Note: This method can unexpectedly return null (leading to an NPE)
        // if the backing dataset's find() method returns quads for which contains() returns false

        Quad result = null;
        Node g = quad.getGraph();
        Node p = quad.getPredicate();
        // Note: Even if either the subjects or objects array has a size of 1
        // we need to perform the contains check to determine which inferrable quad is in the graph

        outer: for (Node s : sortedSubjects) {
            for (Node o : sortedObjects) {
                // System.out.println("Lookup: " + s + " - " + o);

                // Note: We can either create the cross product between subjects and objects
                // Or we e.g. lookup the os for a subjects and find the least one within the objects
                // We could add a threshold to the cross product size as when to resort to graph lookups instead.
                if (getR().contains(g, s, p, o)) {
                    result = Quad.create(g, s, p, o);
                    break outer;
                }
            }
        }
        return result;
    }

    private List<Node> resolveSameAsSortedCached(Node g, Node start, Cache<Entry<Node, Node>, List<Node>> sameAsCache) {
        List<Node> result;
        if (!g.isConcrete() || !start.isConcrete() || start.isLiteral()) {
            result = Collections.singletonList(start);
        } else {
            boolean[] wasComputed = { false };
            Entry<Node, Node> startKey = Map.entry(g, start);
            result = CacheUtils.get(sameAsCache, startKey, () -> { wasComputed[0] = true; return resolveSameAsSorted(g, start); });

            // From experiments it is cheaper to compute the closure for resources
            // only when needed - rather than spending time on preemptively adding cache entries
            boolean cacheClosureForAllMembers = false;
            if (cacheClosureForAllMembers) {
                // Add cache entries for all nodes in the closure
                if (wasComputed[0]) {
                    for (Node node : result) {
                        if (!node.equals(start)) {
                            sameAsCache.put(Map.entry(g, node), result);
                        }
                    }
                    // Ensure that the startKey is marked as recently used
                    sameAsCache.put(startKey, result);
                }
            }
        }
        return result;
    }

    /**
     * Collect the same as closure into a sorted list (non-cached).
     * Always returns an ArrayList. to e.g.
     */
    private List<Node> resolveSameAsSorted(Node g, Node start) {
        List<Node> result = resolveSameAs(g, start).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(result, NodeCmp::compareRDFTerms);
        return result;
    }

    /**
     *
     * @param g
     * @param start
     * @return A stream of unique start's same-as reachable nodes.
     */
    private Iter<Node> resolveSameAs(Node g, Node start) {
        Traverser<Node> traverser = Traverser.forGraph(n -> getDirectNodes(g, n));
        // Note: Traverser always includes the start node in its result
        Iter<Node> result = Iter.iter(traverser.depthFirstPreOrder(start).iterator());
        // result = StreamUtils.viaList(result, list -> System.out.println("resolveSameAs [greaterOrEqual=" + greaterOrEqual + "]: " + start + " -> " + list));
        return result;
    }

    private Set<Node> getDirectNodes(Node g, Node start) {
        Set<Node> result;
        result = start.isLiteral()
            ? Collections.emptySet()
            : loadDirectNodes(g, start);
        return result;
    }


    /** This method should only be invoked by the cache callback */
    private Set<Node> loadDirectNodes(Node g, Node s) {
        Set<Node> result = findDirectTriples(g, s).toSet();
        return result;
    }

    /** Stream direct triples in both directions */
    private Iter<Node> findDirectTriples(Node g, Node s) {
        return Iter.concat(
            Iter.iter(sameAsPredicates).flatMap(p -> findDirectNodes(g, s, p, true)),
            Iter.iter(sameAsPredicates).flatMap(p -> findDirectNodes(g, s, p, false)));
    }

    /** Stream direct triples in a specific direction (ingoing or outgoing) */
    private Iter<Node> findDirectNodes(Node g, Node s, Node p, boolean isForward) {
        Iter<Node> result = isForward
                ? Iter.iter(getR().find(g, s, p, Node.ANY)).map(Quad::getObject)
                : Iter.iter(getR().find(g, Node.ANY, p, s)).map(Quad::getSubject);
        return result;
    }
}
