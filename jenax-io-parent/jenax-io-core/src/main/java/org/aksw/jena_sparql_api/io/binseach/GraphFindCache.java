package org.aksw.jena_sparql_api.io.binseach;

import org.aksw.commons.io.input.ReadableChannelSource;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


/** This cache needs to be put into a dataset's context.
 *  Used by {@link StageGeneratorGraphFindRaw} to cache triple lookups.
 *
 *  ISSUE Needs to be generalized to quads.
 */
public class GraphFindCache {
    public static final String NS = "https://w3id.org/aksw/jenax#graph.";
    public static final Symbol graphCache = SystemARQ.allocSymbol(NS, "cache") ;

    /** Return the global instance (if any) in ARQ.getContex() */
//    public static GraphFindCache get() {
//        return get(ARQ.getContext());
//    }

    public static GraphFindCache get(Context cxt) {
        return cxt.get(graphCache);
    }

//    public static BinSearchResourceCache newDftCache() {
////        if (dftCache == null) {
////            synchronized (GraphFindCache.class) {
////                if (dftCache == null) {
////                    dftCache = new BinSearchResourceCache(512);
////                }
////            }
////        }
////        return dftCache;
//    }

    public static GraphFindCache getOrCreate(Context cxt) {
        GraphFindCache result = get(cxt);
//        if (result == null) {
//            result = getDftCache();
//            set(cxt, result);
//        }
        return result;
    }

    public static void set(Context cxt, GraphFindCache cache) {
        cxt.put(graphCache, cache);
    }

    private Cache<Triple, ReadableChannelSource<Binding[]>> cache;

    public GraphFindCache(int cacheSize) {
        super();
        this.cache = Caffeine.newBuilder().maximumSize(cacheSize).build();
    }

    public GraphFindCache(Cache<Triple, ReadableChannelSource<Binding[]>> cache) {
        super();
        this.cache = cache;
    }

    public Cache<Triple, ReadableChannelSource<Binding[]>> getCache() {
        return cache;
    }
}
