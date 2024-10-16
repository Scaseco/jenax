package org.aksw.jenax.arq.service.vfs;

import org.aksw.commons.io.hadoop.binseach.v2.BinSearchResourceCache;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** A cache that maps resources such as files or http URLs to information for binary search. */
public class ServiceExecutorBinSearch {
    public static final String NS = "https://w3id.org/aksw/jenax#binsearch.";
    public static final Symbol binSearchCache = SystemARQ.allocSymbol(NS, "cache") ;

    private static BinSearchResourceCache dftCache = null;

    /** Return the global instance (if any) in ARQ.getContex() */
    public static BinSearchResourceCache get() {
        return get(ARQ.getContext());
    }

    public static BinSearchResourceCache get(Context cxt) {
        return cxt.get(binSearchCache);
    }

    public static BinSearchResourceCache getDftCache() {
        if (dftCache == null) {
            synchronized (ServiceExecutorBinSearch.class) {
                if (dftCache == null) {
                    dftCache = new BinSearchResourceCache(512);
                }
            }
        }
        return dftCache;
    }

    public static BinSearchResourceCache getOrCreate(Context cxt) {
        BinSearchResourceCache result = get(cxt);
        if (result == null) {
            result = getDftCache();
            set(cxt, result);
        }
        return result;
    }

    public static void set(Context cxt, BinSearchResourceCache cache) {
        cxt.put(binSearchCache, cache);
    }
}
