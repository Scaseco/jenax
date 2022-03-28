package org.aksw.commons.io.seekable.api;

import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.cache.async.AsyncClaimingCacheImpl.Builder;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.Page;
import org.aksw.commons.io.block.impl.PageManagerOverDataStreamSource;
import org.aksw.commons.io.input.DataStreamSource;
import org.aksw.commons.io.seekable.impl.SeekableSourceFromPageManager;

import com.github.benmanes.caffeine.cache.Caffeine;

public class SeekableSources {
    public static SeekableSource of(DataStreamSource<byte[]> dataStreamSource, int pageSize, int pageCacheSize) {
        Builder<Long, Page> builder = AsyncClaimingCacheImpl.newBuilder(
                Caffeine.newBuilder().maximumSize(pageCacheSize));

        PageManager pageManager = new PageManagerOverDataStreamSource(dataStreamSource, pageSize, builder);
        SeekableSource result = SeekableSourceFromPageManager.create(pageManager);
        return result;
    }
}
