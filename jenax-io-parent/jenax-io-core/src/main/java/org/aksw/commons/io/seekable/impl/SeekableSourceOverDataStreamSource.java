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
 * Seekable abstraction over a DataStreamSource.
 *
 * Deprecated because this type of wrapping is too inefficient.
 * Instead first wrap with DataStreamSource with a PageManager and then
 * use a PageNavigator on top of that.
 *
 * @author raven
 *
 */
@Deprecated
public class SeekableSourceOverDataStreamSource
    implements SeekableSource
{
    protected DataStreamSource<byte[]> source;

    // Maximum number of items for which a seek operation performs needless reads rather than
    // starting a new request
    protected long maxSeekByReadLength;

    // Use -1 if the size is unknown
    public SeekableSourceOverDataStreamSource(DataStreamSource<byte[]> source, long maxSeekByReadLength) {
        super();
        this.source = source;
        this.maxSeekByReadLength = maxSeekByReadLength;
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
        return source.size();
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
            this.currentRequestedPos = currentPos;
        }

        @Override
        public boolean isOpen() {
            return !isClosed;
        }

        @Override
        public Seekable clone() {
            return new SeekableFromSequentialReaderSource(currentActualPos);
        }

        /** Ensure that the actual position matches the requested one */
        protected void syncPos() throws IOException {
            if (currentReader != null) {
                int delta = Ints.saturatedCast(currentRequestedPos - currentActualPos);
                if (delta > 0 && delta < maxSeekByReadLength && delta != Integer.MAX_VALUE) {
                    // checkNext(delta, true);
                    skip(delta);
                } else if (currentRequestedPos != currentActualPos) {
                    currentReader.close();
                    currentReader = null;
                }
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
            long size = size();

            boolean result = currentRequestedPos == Long.MAX_VALUE || (size >= 0 && currentRequestedPos >= size);
            return result;
        }

        protected int skip(int len) throws IOException {
            if (skipBuffer == null) {
                skipBuffer = new byte[1024 * 4];
            }

            int sbs = skipBuffer.length;

            int remaining = len;
            int contrib = 0;
            while (
                    contrib >= 0 &&
                    (remaining -= contrib) > 0 &&
                    (contrib = currentReader.read(skipBuffer, 0, Math.min(remaining, sbs))) >= 0) {
            }

            int result = len - remaining;

            currentActualPos += result;
            currentRequestedPos = currentActualPos;

            return result;
        }

        @Override
        public int checkNext(int len, boolean changePos) throws IOException {
            int result;
            long size = size();
            if (size >= 0) {
                long available = size - currentRequestedPos;
                result = Math.max(Ints.saturatedCast(Math.min(len, available)), 0);

                if (changePos) {
                    currentRequestedPos += result;
                }
            } else {
                long savedPos = currentActualPos;

                if (true) {
                    throw new UnsupportedOperationException("unknown size is not yet supported");
                }
            }

            return result;
        }


        @Override
        public int checkPrev(int len, boolean changePos) throws IOException {
            int result = Ints.saturatedCast(Math.min(currentRequestedPos, len));
            if (changePos) {
                currentRequestedPos -= result;
            }
            return result;
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

            if (result >= 0) {
                currentActualPos += result;
            } else {
                currentActualPos = Long.MAX_VALUE;
            }

            currentRequestedPos = currentActualPos;
            return result;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int result;
            int n = dst.remaining();
            if (dst.hasArray()) {
                int dstPos = Ints.checkedCast(dst.position());
                result = read(dst.array(), dstPos, n);
                if (result > 0) {
                    dst.position(dstPos + result);
                }
            } else {
                byte[] buf = new byte[n];
                result = read(buf, 0, n);

                if (result > 0) {
                    dst.put(buf, 0, result);
                }
            }

            return result;
        }


        @Override
        public void closeActual() throws IOException {
            if (currentReader != null) {
                currentReader.close();
            }
        }

        @Override
        public long size() throws IOException {
            long result = SeekableSourceOverDataStreamSource.this.size();
            return result;
        }
    }
}
