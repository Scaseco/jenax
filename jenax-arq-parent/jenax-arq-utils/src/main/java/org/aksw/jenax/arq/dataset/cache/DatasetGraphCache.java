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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperFindBase;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;


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
    private static final Logger logger = LoggerFactory.getLogger(DatasetGraphCache.class);

    public static boolean logCacheStats = false;

    // Just a best effort counter - probably not worth making it volatile
    protected long findCounter = 0;


    /** (CachePattern, In-Tuple) -&gt; Quad */
    protected Supplier<Cache<Entry<Quad, Tuple<Node>>, Set<Quad>>> cacheFactory;
    protected Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache;
    protected AtomicLong cacheVersion = new AtomicLong(-1);
    protected volatile long cacheGeneration = 0;
    // protected boolean isBaseInUdfMode;

    /**
     * In tabling mode all data matching the configured patterns is prefetched.
     * This way, lookups that match the cache patterns can always be answered from the cache alone;
     * especially, if for such a lookup there is no cache entry then no request to the backend needs to be made.
     *
     * If tabling is enabled then the cache should have unlimited size.
     */
    protected boolean isTablingMode = false;


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
        Supplier<Cache<Entry<Quad, Tuple<Node>>, Set<Quad>>> cacheFactory = () ->
                CacheUtils.recordStats(CacheBuilder.newBuilder(), logCacheStats)
                .maximumSize(maxCacheSize)
                .build();

        return new DatasetGraphCache(base, cachePatterns, cacheFactory, isTablingMode);
    }

    protected DatasetGraphCache(DatasetGraph base, Collection<CachePattern> cachePatterns, Supplier<Cache<Entry<Quad, Tuple<Node>>, Set<Quad>>> cacheFactory, boolean isTablingMode) {
        super(base);
        this.cachePatterns = cachePatterns;
        this.cacheFactory = cacheFactory;
        this.isTablingMode = isTablingMode;

        if (!isTablingMode) {
            this.cache = cacheFactory.get();
        }
        // We need to know if the backend adds 'virtual' triples
        // this.isBaseInUdfMode = DatasetGraphUnionDefaultGraph.isKnownUnionDefaultGraphMode(getR());
    }

    public boolean isTablingMode() {
        return isTablingMode;
    }

    public boolean mayContainQuad(Node g, Node s, Node p, Node o) {
        return mayContainQuad(Quad.create(g, s, p, o));
    }

    protected void nextGeneration() {
        ++cacheGeneration;
    }

    @Override
    public void add(Node g, Node s, Node p, Node o)
    {
        getW().add(g, s, p, o);
        nextGeneration();
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        getW().add(g, s, p, o);
        nextGeneration();
    }

    @Override
    public void addAll(DatasetGraph src) {
        getW().addAll(src);
        nextGeneration();
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        getW().deleteAny(g, s, p, o);
        nextGeneration();
    }

    @Override
    public void add(Quad quad) {
        getW().add(quad);
        nextGeneration();
    }

    @Override
    public void delete(Quad quad) {
        getW().delete(quad);
        nextGeneration();
    }

    @Override
    public void abort() {
        nextGeneration();
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
            Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> currentCache = ensureFilledTables();
            for (CachePattern pattern : cachePatterns) {
                if (pattern.subsumes(quad)) {
                    Tuple<Node> key = pattern.createPartitionKey(quad);
                    Set<Quad> partition = CacheUtils.getIfPresent(currentCache, Map.entry(pattern.getSpecPattern(), key));

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
//        if (quad.getGraph().toString().contains("UnionGraph")) {
//            System.err.println("UnionGraph Request");
//        }
//      	System.err.println("Cache: " + cacheVersion + " generation: " + cacheGeneration + " request: " + quad);

//        System.err.println("May contain quad " + quad + " -> " + result + "; cache version: " + cacheVersion + ", generation: " + cacheGeneration);
        return result;
    }

//    public Set<Quad> getCacheEntry(Entry<Quad, Tuple<Node>> key) {
//        return CacheUtils.getIfPresent(cache, key);
//    }


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

            Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> currentCache = ensureFilledTables();
            if (isTablingMode) {
                ensureFilledTables();
                Collection<Quad> bucket = CacheUtils.getIfPresent(currentCache, key);

                result = bucket == null
                        ? Collections.emptyIterator()
                        : bucket.iterator();

//                    if (bucket == null) {
//                        System.out.println("Cache miss on: " + key);
//                    }
            } else {
                result = CacheUtils.get(cache, key, () -> Iter.iter(delegateFind(ng, mg, ms, mp, mo))
                            .collect(Collectors.toCollection(LinkedHashSet::new))).iterator();
            }
        } else {
            result = delegateFind(ng, mg, ms, mp, mo);
        }
        return result;
    }

    public Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> ensureFilledTables() {
        Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> result;
        long ver = cacheVersion.get();
        long gen = cacheGeneration;
        if (ver != gen) {
            if (isTablingMode) {
                // System.out.println("Refresh by " + Thread.currentThread());
                result = refreshTables();
                if (cacheVersion.compareAndSet(ver, gen)) {
                    // Not race condition safe; might need improvement
                    cache = result;
                }
                // System.out.println("Tabled " + StackTraceUtils.toString(Thread.currentThread().getStackTrace()));
            } else {
                cache.invalidateAll();
                cacheVersion.compareAndSet(ver, gen);
                result = cache;
            }
        } else {
            result = cache;
        }
        return result;
    }

    private Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> refreshTables() {
        // Idea: We could add a sanity check that no data gets evicted in the process
        // long expectedCacheSize = 0;

        Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> result = cacheFactory.get();
        for (CachePattern cachePattern : cachePatterns) {
            Quad sp = cachePattern.getSpecPattern();
            Quad baseFp = cachePattern.getFindPattern();

            // Also index the union graph
            List<Quad> lookups = Arrays.asList(baseFp, Quad.create(Quad.unionGraph, baseFp.asTriple()));

            for (Quad fp : lookups) {
                Iterator<Quad> it = delegateFind(false, fp.getGraph(), fp.getSubject(), fp.getPredicate(), fp.getObject());
                try {
                    Set<Node> seenGraphs = new LinkedHashSet<>();
                    long counter = 0;
                    while (it.hasNext()) {
                        Quad quad = it.next();
                        ++counter;

//                        if (fp.getGraph().equals(Quad.unionGraph)) {
//                            System.out.println("udf quad: " + quad);
//                        }

                        Tuple<Node> key = cachePattern.createPartitionKey(quad);
                        Collection<Quad> bucket = CacheUtils.get(result, Map.entry(sp, key), LinkedHashSet::new);
                        bucket.add(quad);
                        seenGraphs.add(quad.getGraph());
                    }
                    logger.info("Tabeling: " + fp + " indexed " + counter + " quads in " + seenGraphs.size() + " graphs, first 100: " + Iterables.limit(seenGraphs, 100));
                    // System.err.println("Tabling; seen graphs: " + seenGraphs);
                } finally {
                    Iter.close(it);
                }
            }
        }
        return result;
    }
}


//
//
//
//@Override public void add(Node g, Node s, Node p, Node o) { add(new Quad(g, s, p, o)); }
//@Override public void delete(Node g, Node s, Node p, Node o) { delete(new Quad(g, s, p, o)); }
//
//@Override
//public void addAll(DatasetGraph src) {
//    addAll(() -> src.stream());
////    try {
////        Lock lock = getLock();
////        lock.enterCriticalSection(Lock.WRITE);
////        try (Stream<Quad> stream = src.stream()) {
////            stream.forEach(quad -> performUpdateAction(quad, false, (ts, t) -> ts.add(t), () -> super.add(quad)));
////        }
////    } finally {
////        getLock().leaveCriticalSection();
////    }
//}
//
//public void addAll(Supplier<Stream<Quad>> streamFactory) {
//    synchronized (tablingLock) {
//        try (Stream<Quad> stream = streamFactory.get()) {
//            CacheUtils.invalidateAll(cache);
//            isTabled = false;
//            stream.forEach(quad -> performUpdateAction(quad, false, true, (ts, t) -> ts.add(t), () -> super.add(quad)));
//        }
//    }
//}
//
//@Override
//public void deleteAny(Node g, Node s, Node p, Node o) {
//    // TODO For simplicity we just invalidate everything
//    synchronized (tablingLock) {
//        CacheUtils.invalidateAll(cache);
//        isTabled = false;
//        super.deleteAny(g, s, p, o);
//    }
//}
//
//@Override
//public void add(Quad quad) {
//    performUpdateAction(quad, true, true, Collection::add, () -> super.add(quad));
//}
//
//@Override
//public void delete(Quad quad) {
//    performUpdateAction(quad, true, true, Collection::remove, () -> super.delete(quad));
//}
//
//
//public void performUpdateAction(Quad quad, boolean doLocking, boolean doInvalidate, BiConsumer<Set<Quad>, Quad> cacheAction, Runnable graphAction) {
//    List<CachePattern> patterns = getMatchingCachePatterns(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()).collect(Collectors.toList());
//    if (!patterns.isEmpty()) {
//        synchronized (tablingLock) {
//            if (doInvalidate) {
//                CacheUtils.invalidateAll(cache);
//                isTabled = false;
//            }
//            graphAction.run();
//        }
//    } else {
//        graphAction.run();
//    }
//}
//
//public void performUpdateActionOld(Quad quad, boolean doLocking, BiConsumer<Set<Quad>, Quad> cacheAction, Runnable graphAction) {
//    List<CachePattern> patterns = getMatchingCachePatterns(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()).collect(Collectors.toList());
//    if (!patterns.isEmpty()) {
//        Lock lock = getLock();
//        if (doLocking) { lock.enterCriticalSection(Lock.WRITE); }
//        try {
//            for (CachePattern cachePattern : cachePatterns) {
//                Tuple<Node> partitionKey = cachePattern.createPartitionKey(quad);
//                Set<Quad> bucket = CacheUtils.getIfPresent(cache, Map.entry(cachePattern.getSpecPattern(), partitionKey));
//                if (bucket != null) {
//                    cacheAction.accept(bucket, quad);
//
//                    // Also cache for graph views
//                    // for (Node g : Arrays.asList(Quad.defaultGraphIRI, Quad.unionGraph)) {
//                        Node ug = Quad.unionGraph;
//                        Quad ugQuad = Quad.create(ug, quad.asTriple());
//                        Tuple<Node> ugKey = cachePattern.createPartitionKey(ugQuad);
//                        Set<Quad> ugBucket = CacheUtils.get(cache, Map.entry(cachePattern.getSpecPattern(), ugKey), LinkedHashSet::new);
//                        cacheAction.accept(ugBucket, quad);
//                    // }
//                }
//            }
//            graphAction.run();
//        } finally {
//            if (doLocking) { lock.leaveCriticalSection(); }
//        }
//    } else {
//        graphAction.run();
//    }
//}
//
//@Override
//public void abort() {
//    synchronized (tablingLock) {
//        CacheUtils.invalidateAll(cache);
//        isTabled = false;
//        super.abort();
//    }
//}

