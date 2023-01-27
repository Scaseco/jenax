package org.aksw.jenax.arq.dataset.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
 * A wrapper that caches the result of {@link #find(Node, Node, Node, Node)} calls for a configurable set of {@link CachePattern}s.
 *
 * Upon caching, the full result set will be immediately consumed and put into the cache.
 * Hence, this cache should only be used with patterns that lead to small result sets.
 *
 * This class may be superseded by a more general framework that allows for specification of arbitrary patterns.
 */
public class DatasetGraphCache
    extends DatasetGraphWrapperFindBase
{
    protected boolean logCacheStats = false;

    protected Cache<Entry<Quad, Tuple<Node>>, Set<Quad>> cache;
    protected Collection<CachePattern> cachePatterns;

    public static final int DFT_MAX_CACHE_SIZE = 10_000;

    public static DatasetGraph cacheByPredicate(DatasetGraph base, Node sameAsPredicate) {
        return cacheByPredicates(base, Collections.singleton(sameAsPredicate), DFT_MAX_CACHE_SIZE);
    }

    public static DatasetGraph cacheByPredicate(DatasetGraph base, Node sameAsPredicate, int maxCacheSize) {
        return cacheByPredicates(base, Collections.singleton(sameAsPredicate), maxCacheSize);
    }

    public static DatasetGraph cacheByPredicates(DatasetGraph base, Set<Node> sameAsPredicates, int maxCacheSize) {
        List<CachePattern> patterns = sameAsPredicates.stream().flatMap(p -> Stream.of(
                CachePattern.create(CachePattern.IN, CachePattern.IN, p, Node.ANY),
                CachePattern.create(CachePattern.IN, Node.ANY, p, CachePattern.IN)
        )).collect(Collectors.toList());

        return cache(base, patterns, maxCacheSize);
    }

    public static DatasetGraph cache(DatasetGraph base, Collection<CachePattern> sameAsPredicates, int maxCacheSize) {
        return new DatasetGraphCache(base, sameAsPredicates, maxCacheSize);
    }

    protected DatasetGraphCache(DatasetGraph base, Collection<CachePattern> cachePatterns, int maxCacheSize) {
        super(base);
        this.cache =
                CacheUtils.recordStats(CacheBuilder.newBuilder(), logCacheStats)
                .maximumSize(maxCacheSize)
                .build();

        this.cachePatterns = cachePatterns;
    }

    @Override public void add(Node g, Node s, Node p, Node o) { add(new Quad(g, s, p, o)); }
    @Override public void delete(Node g, Node s, Node p, Node o) { delete(new Quad(g, s, p, o)); }

    @Override
    public void addAll(DatasetGraph src) {
        try {
            getLock().enterCriticalSection(Lock.WRITE);
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
        getLock().enterCriticalSection(Lock.WRITE);
        try {
            CacheUtils.invalidateAll(cache);
            super.deleteAny(g, s, p, o);
        } finally {
            getLock().leaveCriticalSection();
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
                    ts = CacheUtils.getIfPresent(cache, Map.entry(cachePattern.getPattern(), partitionKey));
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
        // Note: Requesting a readLock if the write lock is already held should simply increment the lock count on the write lock
        getLock().enterCriticalSection(Lock.READ);
        try {
            CacheUtils.invalidateAll(cache);
            super.abort();
        } finally {
            getLock().leaveCriticalSection();
        }
    }

    protected Stream<CachePattern> getMatchingCachePatterns(Node mg, Node ms, Node mp, Node mo) {
        return cachePatterns.stream().filter(pattern -> pattern.matchesPattern(mg, ms, mp, mo));
    }

    @Override
    protected Iterator<Quad> actionFind(boolean ng, Node mg, Node ms, Node mp, Node mo) {
        Iterator<Quad> result;

        CachePattern cachePattern = getMatchingCachePatterns(mg, ms, mp, mo).findFirst().orElse(null);
        if (cachePattern != null) {
            Tuple<Node> partitionKey = cachePattern.createPartitionKey(mg, ms, mp, mo);
            result = CacheUtils.get(cache, Map.entry(cachePattern.getPattern(), partitionKey), () -> Iter.iter(delegateFind(ng, mg, ms, mp, mo)).toSet()).iterator();
        } else {
            result = delegateFind(ng, mg, ms, mp, mo);
        }
        return result;
    }
}
