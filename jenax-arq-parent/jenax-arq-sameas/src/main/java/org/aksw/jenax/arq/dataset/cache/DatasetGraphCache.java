package org.aksw.jenax.arq.dataset.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.cache.CacheUtils;
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

    protected Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache;

    /**
     * In tabling mode all data matching the configured patterns is prefetched.
     * This way, lookups that match the cache patterns can always be answered from the cache alone;
     * especially, if for such a lookup there is no cache entry then no request to the backend needs to be made.
     *
     * If tabling is enabled then the cache should have unlimited size.
     */
    protected boolean isTablingMode = false;

    /**
     * Whether the cache patterns have been tabeled.
     * This flag could be maintained on a per-pattern bases */
    protected volatile boolean isTabled = false;

    protected Collection<CachePattern> cachePatterns;

    public static final int DFT_MAX_CACHE_SIZE = 10_000;

    public static DatasetGraph cache(DatasetGraph base, Collection<CachePattern> cachePatterns) {
        return cache(base, cachePatterns, DFT_MAX_CACHE_SIZE);
    }

    public static DatasetGraph cache(DatasetGraph base, Collection<CachePattern> cachePatterns, int maxCacheSize) {
        return create(base, cachePatterns, maxCacheSize, false);
    }

    public static DatasetGraph table(DatasetGraph base, CachePattern cachePatterns) {
        return table(base, Collections.singletonList(cachePatterns));
    }

    public static DatasetGraph table(DatasetGraph base, Collection<CachePattern> cachePatterns) {
        return create(base, cachePatterns, Long.MAX_VALUE, true);
    }

    public static DatasetGraph create(DatasetGraph base, Collection<CachePattern> cachePatterns, long maxCacheSize, boolean isTablingMode) {
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
    }

    @Override public void add(Node g, Node s, Node p, Node o) { add(new Quad(g, s, p, o)); }
    @Override public void delete(Node g, Node s, Node p, Node o) { delete(new Quad(g, s, p, o)); }

    @Override
    public void addAll(DatasetGraph src) {
        try {
            Lock lock = getLock();
            lock.enterCriticalSection(Lock.WRITE);
            try (Stream<Quad> stream = src.stream()) {
                stream.forEach(quad -> performUpdateAction(quad, false, (ts, t) -> ts.add(t), () -> super.add(quad)));
            }
        } finally {
            getLock().leaveCriticalSection();
        }
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        // TODO For simplicity we just invalidate everything
        Lock lock = getLock();
        lock.enterCriticalSection(Lock.WRITE);
        try {
            CacheUtils.invalidateAll(cache);
            isTabled = false;
            super.deleteAny(g, s, p, o);
        } finally {
            lock.leaveCriticalSection();
        }
    }

    @Override
    public void add(Quad quad) {
        performUpdateAction(quad, true, Collection::add, () -> super.add(quad));
    }

    @Override
    public void delete(Quad quad) {
        performUpdateAction(quad, true, Collection::remove, () -> super.delete(quad));
    }

    public void performUpdateAction(Quad quad, boolean doLocking, BiConsumer<Set<Quad>, Quad> cacheAction, Runnable graphAction) {
        Set<Quad> ts;

        List<CachePattern> patterns = getMatchingCachePatterns(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()).collect(Collectors.toList());
        if (!patterns.isEmpty()) {
            if (doLocking) { getLock().enterCriticalSection(Lock.WRITE); }
            try {
                for (CachePattern cachePattern : cachePatterns) {
                    Tuple<Node> partitionKey = cachePattern.createPartitionKey(quad);
                    ts = CacheUtils.getIfPresent(cache, Map.entry(cachePattern.getSpecPattern(), partitionKey));
                    if (ts != null) {
                        cacheAction.accept(ts, quad);
                    }
                }
                graphAction.run();
            } finally {
                if (doLocking) { getLock().leaveCriticalSection(); }
            }
        } else {
            graphAction.run();
        }
    }

    @Override
    public void abort() {
        Lock lock = getLock();
        lock.enterCriticalSection(Lock.WRITE);
        try {
            CacheUtils.invalidateAll(cache);
            isTabled = false;
            super.abort();
        } finally {
            lock.leaveCriticalSection();
        }
    }

    protected Stream<CachePattern> getMatchingCachePatterns(Node mg, Node ms, Node mp, Node mo) {
        return cachePatterns.stream().filter(pattern -> pattern.matchesPattern(mg, ms, mp, mo));
    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node mg, Node ms, Node mp, Node mo) {
        Iterator<Quad> result;

        if (logCacheStats) {
            ++findCounter;
            if (findCounter % 100000 == 0) {
                System.out.println(CacheUtils.stats(cache));
            }
        }

        ensureReady();
        CachePattern cachePattern = getMatchingCachePatterns(mg, ms, mp, mo).findFirst().orElse(null);

        if (cachePattern != null) {
            Tuple<Node> partitionKey = cachePattern.createPartitionKey(mg, ms, mp, mo);
            Entry<Quad, Tuple<Node>> key = Map.entry(cachePattern.getSpecPattern(), partitionKey);

            if (isTablingMode) {
                Lock lock = getR().getLock();
                lock.enterCriticalSection(Lock.READ);
                try {
                    Collection<Quad> bucket = CacheUtils.getIfPresent(cache , key);
//                    if (bucket == null) {
//                        System.out.println("Cache miss on: " + key);
//                    }
                    result = bucket == null ? Collections.emptyIterator() : bucket.iterator();
                } finally {
                    lock.leaveCriticalSection();
                }
            } else {
                result = CacheUtils.get(cache, key, () -> Iter.iter(delegateFind(ng, mg, ms, mp, mo)).toSet()).iterator();
            }
        } else {
            result = delegateFind(ng, mg, ms, mp, mo);
        }
        return result;
    }

    public void ensureReady() {
        if (isTablingMode && !isTabled) {
            Lock lock = getR().getLock();
            lock.enterCriticalSection(Lock.WRITE); // Block other reads
            try {
                if (!isTabled) {
                    // System.out.println("Refresh by " + Thread.currentThread());
                    refreshTables();
                    isTabled = true;
                }
            } finally {
                lock.leaveCriticalSection();
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
                while (it.hasNext()) {
                    Quad quad = it.next();
                    Tuple<Node> key = cachePattern.createPartitionKey(quad);
                    Collection<Quad> bucket = CacheUtils.get(cache, Map.entry(sp, key), LinkedHashSet::new);
                    bucket.add(quad);
                }
            } finally {
                Iter.close(it);
            }
        }
    }
}
