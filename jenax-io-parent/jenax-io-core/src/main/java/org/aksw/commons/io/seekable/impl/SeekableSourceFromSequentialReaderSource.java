package org.aksw.commons.io.seekable.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.aksw.commons.io.input.DataStream;
import org.aksw.commons.io.input.DataStreamSource;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.io.seekable.api.SeekableSource;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.nimbusds.jose.util.StandardCharset;


/**
 * Seekable abstraction over a DataStream
 *
 * @author raven
 *
 */
public class SeekableSourceFromSequentialReaderSource
    implements SeekableSource
{
    protected DataStreamSource<byte[]> source;
    protected long knownSize;

    // Maximum number of items for which a seek operation performs needless reads rather than
    // starting a new request
    protected long maxSeekByReadLength;

    // Use -1 if the size is unknown
    public SeekableSourceFromSequentialReaderSource(DataStreamSource<byte[]> source, long knownSize, long maxSeekByReadLength) {
        super();
        this.source = source;
        this.maxSeekByReadLength = maxSeekByReadLength;
        this.knownSize = knownSize;
    }

    @Override
    public boolean supportsAbsolutePosition() {
        return true;
    }

    @Override
    public Seekable get(long pos) throws IOException {
        return new SeekableFromSequentialReaderSource(pos);
    }

    @Override
    public long size() throws IOException {
        return knownSize;
    }


    class SeekableFromSequentialReaderSource
        extends AutoCloseableWithLeakDetectionBase
        implements Seekable
    {
        protected DataStream<byte[]> currentReader;

        protected long currentRequestedPos;

        // actual pos is synced with current request pos and updated after read
        protected long currentActualPos;

        // Buffer for use with the checkNext methods. Initialized once when needed.
        protected byte[] skipBuffer = null;

        public SeekableFromSequentialReaderSource(long currentPos) {
            super();
            this.currentActualPos = currentPos;
        }

        @Override
        public boolean isOpen() {
            return !isClosed;
        }

        @Override
        public Seekable clone() {
            return new SeekableFromSequentialReaderSource(currentActualPos);
        }

        protected void syncPos() throws IOException {
            int delta = Ints.saturatedCast(currentRequestedPos - currentActualPos);
            if (delta > 0 && delta < maxSeekByReadLength && delta != Integer.MAX_VALUE) {
                checkNext(delta, true);
            } else if (currentRequestedPos != currentActualPos) {
                currentReader.close();
                currentReader = null;
            }
        }

        @Override
        public long getPos() throws IOException {
            return currentRequestedPos;
        }

        @Override
        public void setPos(long requestedPos) throws IOException {
            this.currentRequestedPos = requestedPos;
        }

        @Override
        public void posToStart() throws IOException {
            if (currentReader != null) {
                currentReader.close();
                currentReader = null;
                currentRequestedPos = -1;
            }
        }

        @Override
        public void posToEnd() throws IOException {
            if (currentReader != null) {
                currentReader.close();
                currentReader = null;
                currentRequestedPos = Long.MAX_VALUE;
            }
        }

        @Override
        public boolean isPosBeforeStart() throws IOException {
            return currentRequestedPos < 0;
        }

        @Override
        public boolean isPosAfterEnd() throws IOException {
            return currentRequestedPos == Long.MAX_VALUE;
        }

        @Override
        public int checkNext(int len, boolean changePos) throws IOException {
            long savedPos = currentActualPos;

            int sbs = skipBuffer.length;
            if (skipBuffer == null) {
                skipBuffer = new byte[1024 * 4];
            }

            int remaining = len;
            int contrib = 0;
            while (
                    contrib >= 0 &&
                    (remaining -= contrib) > 0 &&
                    (contrib = read(skipBuffer, 0, Math.min(remaining, sbs))) >= 0) {
            }

            int result = len - remaining;

            if (!changePos) {
                setPos(savedPos);
            }

            return result;
        }

        @Override
        public int checkPrev(int len, boolean changePos) throws IOException {
            throw new UnsupportedOperationException("not supported");
        }

        @Override
        public String readString(int len) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(len);
            int n = read(buf);
            byte[] bytes = buf.array();
            String result = new String(bytes, 0, (n < 0 ? 0 : n), StandardCharset.UTF_8);
            return result;
        }

        protected int read(byte[] dst, int offset, int length) throws IOException {
            syncPos();
            if (currentReader == null) {
                currentReader = source.newDataStream(Range.atLeast(currentRequestedPos));
                currentActualPos = currentRequestedPos;
            }

            int result = currentReader.read(dst, offset, length);

            if (result == -1) {
                currentActualPos = Long.MAX_VALUE;
            }
            currentActualPos += result;

            currentRequestedPos = currentActualPos;
            return result;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int r;
            int n = dst.remaining();
            if (dst.hasArray()) {
                r = read(dst.array(), dst.position(), n);
                if (r >= 0) {
                    dst.position(dst.position() + r);
                }
            } else {
                byte[] buf = new byte[n];
                r = read(buf, 0, n);

                if (r >= 0) {
                    dst.put(buf, 0, r);
                }
            }

            return r;
        }


        @Override
        public void closeActual() throws IOException {
            if (currentReader != null) {
                currentReader.close();
            }
        }

    }
}
