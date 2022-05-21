package org.aksw.commons.io.block.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.cache.plain.ClaimingCache;
import org.aksw.commons.cache.plain.ClaimingCacheOverAsync;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.util.range.PageHelper;
import org.aksw.commons.util.ref.Ref;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

public class PageManagerOverDataStreamSource
    implements PageManager
{
    protected ReadableChannelSource<byte[]> source;
    protected int pageSize;
    protected ClaimingCache<Long, Page> pageCache;

    // Cached attributes
    protected long sourceSize;
    protected long lastPageId;

    public PageManagerOverDataStreamSource(
            ReadableChannelSource<byte[]> source, int pageSize, AsyncClaimingCacheImpl.Builder<Long, Page> cacheBuilder) {
        super();
        this.source = source;
        this.pageSize = pageSize;

        try {
            this.sourceSize = source.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.lastPageId = PageHelper.getLastPageId(sourceSize, pageSize);

        this.pageCache = ClaimingCacheOverAsync.wrap(cacheBuilder
                .setCacheLoader(this::loadPage)
                .build());
    }

    @Override
    public Ref<? extends Page> requestBufferForPage(long pageId) {
        return pageCache.claim(pageId);
    }

    public Page loadPage(long pageId) {
        Preconditions.checkArgument(pageId >= 0 && pageId <= lastPageId, "PageId %s out of bounds", pageId);

        Page result;
        long offset = PageHelper.getPageOffsetForPageId(pageId, pageSize);

        // Adjust the size of the last page
        int effectivePageSize = pageId != lastPageId ? pageSize : (int)(sourceSize % pageSize);

        try (ReadableByteChannel channel = ReadableChannels.newChannel(
                source.newReadableChannel(Range.closedOpen(offset, offset + effectivePageSize)))) {

            ByteBuffer buffer = ByteBuffer.allocate(effectivePageSize);
            ByteBuffer dup = buffer.duplicate();
            IOUtils.read(channel, dup);

            result = new PageBase(this, pageId, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getEndPos() {
        return sourceSize;
//        try {
//            long result = source.size();
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}
