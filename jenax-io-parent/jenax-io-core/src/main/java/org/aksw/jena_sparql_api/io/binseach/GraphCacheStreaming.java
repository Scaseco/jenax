package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.CloseableIterator;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.collect.Streams;
import com.google.common.util.concurrent.MoreExecutors;

public class GraphCacheStreaming {

    // XXX Pass executor service via context
    // ForkJoinPool.commonPool();
    protected static ExecutorService globalExecutorService = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());

    public static QueryIterator cache(GraphFindCache cache, Triple lookupPattern, Function<Triple, QueryIterator> itSupp) {
        ReadableChannelSource<Binding[]> cachedSource = cache.getCache().get(lookupPattern, lp -> {

            Supplier<Stream<Binding>> bindingStreamFactory = () -> {
                QueryIterator qIter = itSupp.apply(lp);
                Stream<Binding> stream = Streams.stream(qIter).onClose(qIter::close);
                return stream;
            };

            ReadableChannelSource<Binding[]> source = ReadableChannelSources.ofStreamFactory(bindingStreamFactory);
            // ReadableChannelSource<Binding[]> r = ReadableChannelSources.cacheInMemory(source, 32, 1000, 32);

            ReadableChannelSource<Binding[]> r = AdvancedRangeCacheImpl.<Binding[]>newBuilder()
                    .setDataSource(source)
                    .setWorkerBulkSize(32)
                    .setSlice(SliceInMemoryCache.create(source.getArrayOps(), 1024, 1000))
                    .setRequestLimit(Long.MAX_VALUE) // One worker can serve as much as it wants
                    .setTerminationDelay(Duration.ofSeconds(180)) // TODO BUG - the worker seems to shut down after the delay even while processing
                    .setExecutorService(globalExecutorService)
                    .build();

            return r;

        });
        CloseableIterator<Binding> cit;
        try {
            cit = ReadableChannels.newIterator(cachedSource.newReadableChannel(), 16);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        QueryIterator result = QueryIterPlainWrapper.create(Iter.onClose(cit,  cit::close));
        return result;
    }
}
