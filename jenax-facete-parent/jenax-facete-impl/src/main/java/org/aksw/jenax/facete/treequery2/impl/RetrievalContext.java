package org.aksw.jenax.facete.treequery2.impl;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.io.slice.SliceAccessor;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.aksw.commons.util.cache.CacheUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;

import com.google.common.cache.Cache;

interface RelationContext {
    Slice<Binding> get(Op op);
}

class CacheKey {
    protected Op op;
    protected Binding binding;

    public CacheKey(Op op, Binding binding) {
        super();
        this.op = op;
        this.binding = binding;
    }

    public Op getOp() {
        return op;
    }

    public Binding getBinding() {
        return binding;
    }
}

class RelationContextImpl {
    protected Op op;
    protected Element baseElement;

}

/**
 * A data structure to hold which values were retrieved for which relation under which bindings.
 */
public class RetrievalContext {
    protected Cache<CacheKey, Slice<Binding[]>> cache;

    public Slice<Binding[]> getSlice(Op op, Binding binding) {
        CacheKey cacheKey = new CacheKey(op, binding);
        return CacheUtils.get(cache, cacheKey, () -> createSlice());
    }

    public Slice<Binding[]> createSlice() {
        int pageSize = 50000;
        int maxPageCount = 1000;
        Slice<Binding[]> slice = SliceInMemoryCache.create(ArrayOps.createFor(Binding.class), pageSize, maxPageCount);
        // ServiceCacheValue r = new ServiceCacheValue(id, slice);
        return slice;
    }

    public static void main(String[] args) {
        Slice<Binding[]> slice = null;
        try(SliceAccessor<Binding[]> accessor = slice.newSliceAccessor()) {

        }
    }
}
