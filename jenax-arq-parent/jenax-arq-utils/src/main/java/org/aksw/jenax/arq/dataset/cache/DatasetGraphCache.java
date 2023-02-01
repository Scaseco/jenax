package org.aksw.jenax.arq.dataset.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.commons.util.stack_trace.StackTraceUtils;
import org.aksw.jenax.arq.util.dataset.DatasetGraphUnionDefaultGraph;
import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperFindBase;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * A wrapper that caches the result of {@link #find(Node, Node, Node, Node)} calls
 * for a configurable set of {@link CachePattern}s.
 *
 * This implementation does not perform any subsumption checks.
 * A cache for (IN IN owl:sameAs ANY) is presently not applicable for a lookup
 * with e.g. (foo bar owl:sameAs baz) because baz is more specific than ANY.
 *
 * Upon caching, the full result set will be immediately consumed and put into the cache.
 * Hence, this cache should only be used with patterns that lead to small result sets.
 */
public class DatasetGraphCache
    extends DatasetGraphWrapperFindBase
{
    public static boolean logCacheStats = false;

    // Just a best effort counter - probably not worth making it volatile
    protected long findCounter = 0;


    /** (CachePattern, In-Tuple) -&gt; Quad */
    protected Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache;

    /**
     * In tabling mode all data matching the configured patterns is prefetched.
     * This way, lookups that match the cache patterns can always be answered from the cache alone;
     * especially, if for such a lookup there is no cache entry then no request to the backend needs to be made.
     *
     * If tabling is enabled then the cache should have unlimited size.
     */
    protected boolean isTablingMode = false;

    // protected boolean isBaseInUdfMode;

    /**
     * Whether the cache patterns have been tabeled.
     * This flag could be maintained on a per-pattern bases */
    protected volatile boolean isTabled = false;

    protected Object tablingLock = new Object();

    protected Collection<CachePattern> cachePatterns;

    public static final int DFT_MAX_CACHE_SIZE = 10_000;

    public static DatasetGraphCache cache(DatasetGraph base, Collection<CachePattern> cachePatterns) {
        return cache(base, cachePatterns, DFT_MAX_CACHE_SIZE);
    }

    public static DatasetGraphCache cache(DatasetGraph base, Collection<CachePattern> cachePatterns, int maxCacheSize) {
        return create(base, cachePatterns, maxCacheSize, false);
    }

    public static DatasetGraphCache table(DatasetGraph base, CachePattern cachePatterns) {
        return table(base, Collections.singletonList(cachePatterns));
    }

    public static DatasetGraphCache table(DatasetGraph base, Collection<CachePattern> cachePatterns) {
        return create(base, cachePatterns, Long.MAX_VALUE, true);
    }

    public static DatasetGraphCache create(DatasetGraph base, Collection<CachePattern> cachePatterns, long maxCacheSize, boolean isTablingMode) {
        Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache =
                CacheUtils.recordStats(CacheBuilder.newBuilder(), logCacheStats)
                .maximumSize(maxCacheSize)
                .build();

        return new DatasetGraphCache(base, cachePatterns, cache, isTablingMode);
    }

    protected DatasetGraphCache(DatasetGraph base, Collection<CachePattern> cachePatterns, Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache, boolean isTablingMode) {
        super(base);
        this.cachePatterns = cachePatterns;
        this.cache = cache;
        this.isTablingMode = isTablingMode;

        // We need to know if the backend adds 'virtual' triples
        // this.isBaseInUdfMode = DatasetGraphUnionDefaultGraph.isKnownUnionDefaultGraphMode(getR());
    }

    public boolean isTablingMode() {
        return isTablingMode;
    }

    public boolean mayContainQuad(Node g, Node s, Node p, Node o) {
        return mayContainQuad(Quad.create(g, s, p, o));
    }

    /**
     * This method always returns true unless in tabeling mode.
     * If false then the wrapped dataset does not contain this quad.
     * If the argument quad contains placeholders only a check is made for whether the partition key exists in the cache. The set of cached entries is NOT iterated.
     */
    public boolean mayContainQuad(Quad quad) {
        boolean result = true;
        if (isTablingMode) {
            // Quad quad = getEffectiveQuad(rawQuad);
            synchronized (tablingLock) {
                ensureFilledTables();
                for (CachePattern pattern : cachePatterns) {
                    if (pattern.subsumes(quad)) {
                        Tuple<Node> key = pattern.createPartitionKey(quad);
                        Set<Quad> partition = CacheUtils.getIfPresent(cache, Map.entry(pattern.getSpecPattern(), key));

                        if (partition == null || partition.isEmpty()) {
                            result = false;
                            break;
                        }

                        if (quad.isConcrete()) {
                            result = partition.contains(quad);
                            if (!result) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        // System.err.println("May contain quad " + quad + " -> " + result);
        return result;
    }

//    public Set<Quad> getCacheEntry(Entry<Quad, Tuple<Node>> key) {
//        return CacheUtils.getIfPresent(cache, key);
//    }

    @Override public void add(Node g, Node s, Node p, Node o) { add(new Quad(g, s, p, o)); }
    @Override public void delete(Node g, Node s, Node p, Node o) { delete(new Quad(g, s, p, o)); }

    @Override
    public void addAll(DatasetGraph src) {
        addAll(() -> src.stream());
//        try {
//            Lock lock = getLock();
//            lock.enterCriticalSection(Lock.WRITE);
//            try (Stream<Quad> stream = src.stream()) {
//                stream.forEach(quad -> performUpdateAction(quad, false, (ts, t) -> ts.add(t), () -> super.add(quad)));
//            }
//        } finally {
//            getLock().leaveCriticalSection();
//        }
    }

    public void addAll(Supplier<Stream<Quad>> streamFactory) {
        synchronized (tablingLock) {
            try (Stream<Quad> stream = streamFactory.get()) {
                CacheUtils.invalidateAll(cache);
                isTabled = false;
                stream.forEach(quad -> performUpdateAction(quad, false, true, (ts, t) -> ts.add(t), () -> super.add(quad)));
            }
        }
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        // TODO For simplicity we just invalidate everything
        synchronized (tablingLock) {
            CacheUtils.invalidateAll(cache);
            isTabled = false;
            super.deleteAny(g, s, p, o);
        }
    }

    @Override
    public void add(Quad quad) {
        performUpdateAction(quad, true, true, Collection::add, () -> super.add(quad));
    }

    @Override
    public void delete(Quad quad) {
        performUpdateAction(quad, true, true, Collection::remove, () -> super.delete(quad));
    }


    public void performUpdateAction(Quad quad, boolean doLocking, boolean doInvalidate, BiConsumer<Set<Quad>, Quad> cacheAction, Runnable graphAction) {
        List<CachePattern> patterns = getMatchingCachePatterns(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()).collect(Collectors.toList());
        if (!patterns.isEmpty()) {
            synchronized (tablingLock) {
                if (doInvalidate) {
                    CacheUtils.invalidateAll(cache);
                    isTabled = false;
                }
                graphAction.run();
            }
        } else {
            graphAction.run();
        }
    }

    public void performUpdateActionOld(Quad quad, boolean doLocking, BiConsumer<Set<Quad>, Quad> cacheAction, Runnable graphAction) {
        List<CachePattern> patterns = getMatchingCachePatterns(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()).collect(Collectors.toList());
        if (!patterns.isEmpty()) {
            Lock lock = getLock();
            if (doLocking) { lock.enterCriticalSection(Lock.WRITE); }
            try {
                for (CachePattern cachePattern : cachePatterns) {
                    Tuple<Node> partitionKey = cachePattern.createPartitionKey(quad);
                    Set<Quad> bucket = CacheUtils.getIfPresent(cache, Map.entry(cachePattern.getSpecPattern(), partitionKey));
                    if (bucket != null) {
                        cacheAction.accept(bucket, quad);

                        // Also cache for graph views
                        // for (Node g : Arrays.asList(Quad.defaultGraphIRI, Quad.unionGraph)) {
                            Node ug = Quad.unionGraph;
                            Quad ugQuad = Quad.create(ug, quad.asTriple());
                            Tuple<Node> ugKey = cachePattern.createPartitionKey(ugQuad);
                            Set<Quad> ugBucket = CacheUtils.get(cache, Map.entry(cachePattern.getSpecPattern(), ugKey), LinkedHashSet::new);
                            cacheAction.accept(ugBucket, quad);
                        // }
                    }
                }
                graphAction.run();
            } finally {
                if (doLocking) { lock.leaveCriticalSection(); }
            }
        } else {
            graphAction.run();
        }
    }

    @Override
    public void abort() {
        synchronized (tablingLock) {
            CacheUtils.invalidateAll(cache);
            isTabled = false;
            super.abort();
        }
    }

    protected Stream<CachePattern> getMatchingCachePatterns(Node mg, Node ms, Node mp, Node mo) {
        return cachePatterns.stream().filter(pattern -> pattern.matchesPattern(mg, ms, mp, mo));
    }

    protected Stream<CachePattern> getSuperPatterns(Node mg, Node ms, Node mp, Node mo) {
        return cachePatterns.stream().filter(pattern -> pattern.subsumes(mg, ms, mp, mo));
    }

//
//    protected Quad getEffectiveQuad(Quad quad) {
//        Node g = quad.getGraph();
//        Node eg = getEffectiveGraph(g);
//
//        return g.equals(eg) ? quad : Quad.create(eg, quad.asTriple());
//    }
//
//    protected Node getEffectiveGraph(Node mg) {
//        // Effective match graph
//        Node emg = isBaseInUdfMode && Quad.isDefaultGraph(mg)
//                ? Quad.unionGraph
//                : mg;
//        return emg;
//    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node mg, Node ms, Node mp, Node mo) {
        Iterator<Quad> result;

        if (logCacheStats) {
            ++findCounter;
            if (findCounter % 100000 == 0) {
                System.err.println(CacheUtils.stats(cache));
            }
        }

        // Effective match graph
        // Node emg = getEffectiveGraph(mg);

        // System.out.println("Find action");
        // If tabeling is enabled then ensure all data is prefetched prior to find
        CachePattern cachePattern = getMatchingCachePatterns(mg, ms, mp, mo).findFirst().orElse(null);

        if (cachePattern != null) {
            Tuple<Node> partitionKey = cachePattern.createPartitionKey(mg, ms, mp, mo);
            Entry<Quad, Tuple<Node>> key = Map.entry(cachePattern.getSpecPattern(), partitionKey);

            if (isTablingMode) {
//                Lock lock = getLock();
//                lock.enterCriticalSection(Lock.READ);
//                try {
                    Collection<Quad> bucket;
                    synchronized (tablingLock) {
                        ensureFilledTables();
                        bucket = CacheUtils.getIfPresent(cache, key);
                    }

                    result = bucket == null
                            ? Collections.emptyIterator()
                            : bucket.iterator();

//                    if (bucket == null) {
//                        System.out.println("Cache miss on: " + key);
//                    }
//                } finally {
//                    lock.leaveCriticalSection();
//                }
            } else {
                result = CacheUtils.get(cache, key, () -> Iter.iter(delegateFind(ng, mg, ms, mp, mo))
                            .collect(Collectors.toCollection(LinkedHashSet::new))).iterator();
            }
        } else {
            result = delegateFind(ng, mg, ms, mp, mo);
        }
        return result;
    }

    public void ensureFilledTables() {
        // isTabled = false;
        if (isTablingMode) {
            // TODO Ideally we'd like to check 'isTabled' with a read lock and if false then promote it to write
            if (!isTabled) {
                // System.out.println("Refresh by " + Thread.currentThread());
                refreshTables();
                // System.out.println("Tabled " + StackTraceUtils.toString(Thread.currentThread().getStackTrace()));
                isTabled = true;
            }
        }
    }

    private void refreshTables() {
        // Idea: We could add a sanity check that no data gets evicted in the process
        // long expectedCacheSize = 0;
        for (CachePattern cachePattern : cachePatterns) {
            Quad sp = cachePattern.getSpecPattern();
            Quad fp = cachePattern.getFindPattern();
            Iterator<Quad> it = delegateFind(false, fp.getGraph(), fp.getSubject(), fp.getPredicate(), fp.getObject());
            try {
                Set<Node> seenGraphs = new LinkedHashSet<>();
                while (it.hasNext()) {
                    Quad quad = it.next();
                    Tuple<Node> key = cachePattern.createPartitionKey(quad);
                    Collection<Quad> bucket = CacheUtils.get(cache, Map.entry(sp, key), LinkedHashSet::new);
                    bucket.add(quad);
                    seenGraphs.add(quad.getGraph());

                    // Also cache for graph views
//                    if (isBaseInUdfMode) {
//                        Node ug = Quad.unionGraph;
//                        Quad ugQuad = Quad.create(ug, quad.asTriple());
//                        Tuple<Node> ugKey = cachePattern.createPartitionKey(ugQuad);
//                        Set<Quad> ugBucket = CacheUtils.get(cache, Map.entry(cachePattern.getSpecPattern(), ugKey), LinkedHashSet::new);
//                        ugBucket.add(quad);
//                    }
                }
                // System.err.println("Tabling; seen graphs: " + seenGraphs);
            } finally {
                Iter.close(it);
            }
        }
    }
}
