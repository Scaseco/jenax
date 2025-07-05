package org.aksw.jenax.rdf.io.lenient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.aksw.commons.io.buffer.ring.RingBufferForBytes;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
/*
 * LenientRDFParser.java
 *
 * A wrapper around Apache Jena’s RIOT parser that provides lenient, line‑wise
 * recovery for N‑Triples and N‑Quads streams.  Valid lines are handed directly
 * to Jena for maximum speed; syntax errors are intercepted and reported via an
 * error‑callback, and parsing automatically resumes on the next line.
 *
 * Example usage:
 *
 *     LenientRDFParser parser = LenientRDFParser.forLang(Lang.NTRIPLES)
 *         .onError((lineNo, text) ->
 *             System.err.printf("bad line %d: %s%n", lineNo, text));
 *
 *     try (ReadableByteChannel ch = Files.newByteChannel(Paths.get("data.nt"))) {
 *         parser.parse(ch, StreamRDFLib.writer(System.out));
 *     }
 *
 * The class guarantees that every line is either emitted exactly once via the
 * StreamRDF sink or reported once to the error‑callback, no matter how many
 * times the underlying Jena parser bails out.
 */
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.IteratorParsers;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;

import com.google.common.primitives.Ints;

// Open todos:
// - The line tracker needs a circular buffer to track newlines in requested reads.
// - Line offsets into the circular buffer must be tracked.
// - [done] If the buffer runs full due to very long lines, then propably just double the buffer size.
//     (alternatively, one could use multiple buffers like in the BufferOverInputStream class.)
// - [done] Blank lines must be handled (parser only sends events for non-blank lines)


//so we need to extract the first line in isolation.
//
//if this succeeds to parse in isolation, then the error MUST be with the other line.
//  start the parser with the buffer set to the line AFTER THE NEXT.
//
//if this line fails to parse, then we haven't parsed the next line yet.
//  start the parser with the NEXT line.
//
// channel.getFirstLineBuffer();
// channel
public class UnfinishedLenientRDFParser {

    public static void main(String[] args) throws IOException {
//		RDFParser.create()
//			.fromString("""
//			""")
//			.lang(Lang.NTRIPLES)

        // So an issue are lines that mix valid data with garbage.
        // If the parser does not do much read ahead, then the
        // line would get confirmed, and before the \n token it would also get rejected.
        // this would work.
        // the problem is, if the current line was confirmed, and the parser already read
        // into the next line. in this case, the garbage would be related to next line
        // and this would incorrectly get rejected instead.
        // One option could be, to stop reads on the '\n' token.
        // But this is not reliable because a parser may use a readyFully approach to fill
        // its internal buffers.


        String str = """
            <urn:s> <urn:p> <urn:o> . GARBAGE

            here be dragons .


            <urn:this> <urn:is> <urn:valid> .

            more dragons.

            <urn:valid> <urn:once> "more" .

            # comment

            error

            """;

        try (InputStream in = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {

            ReadableByteChannel channel = Channels.newChannel(in);
            NewlineTrackingChannel tracker = new NewlineTrackingChannel(channel);

            while (true) {
                try {
                    InputStream in2 = Channels.newInputStream(tracker);

                    Iterator<Triple> it = IteratorParsers.createIteratorNTriples(in2);
                    while (it.hasNext()) {
                        Triple t = it.next();
                        System.out.println("Confirming: " + t);
                        tracker.confirmData();
                    }
                } catch (Exception e) {
                    System.out.println("got: " + tracker.extractFirstLine().toString());

                    System.err.println("Error during parse, skipping.");
                    e.printStackTrace();
                    tracker.skipUntilAfterNextNonBlankLineStart();
                    continue;
                }

                break;
            }
        } catch (RiotException e) {
            e.printStackTrace();
        }
    }


    /** Functional interface for reporting invalid lines. */
    @FunctionalInterface
    public interface ErrorCallback extends BiConsumer<Long, String> {}

    /* ------------------------------------------------------------------ */
    private final Lang lang;
    private ErrorCallback errorCallback = (line, text) -> {};

    private UnfinishedLenientRDFParser(Lang lang) {
        if (!(Lang.NTRIPLES.equals(lang) || Lang.NQUADS.equals(lang)))
            throw new IllegalArgumentException("Only N‑Triples and N‑Quads are supported");
        this.lang = lang;
    }

    /** Factory. */
    public static UnfinishedLenientRDFParser forLang(Lang lang) {
        return new UnfinishedLenientRDFParser(Objects.requireNonNull(lang));
    }

    /** Register an error callback. */
    public UnfinishedLenientRDFParser onError(ErrorCallback cb) {
        this.errorCallback = Objects.requireNonNull(cb);
        return this;
    }

    /**
     * Parses {@code src} into {@code sink}.
     * <p>The method never throws for malformed input – every syntactically
     * invalid line is passed to {@code errorCallback} and parsing continues
     * with the next line.</p>
     */
    public void parse(ReadableByteChannel src, StreamRDF sink) throws IOException {
        NewlineTrackingChannel tracking = new NewlineTrackingChannel(src);

        while (tracking.isOpen()) {
            CountingStreamRDF countingSink = new CountingStreamRDF(sink, tracking);
            // long linesSeenBefore = tracking.getLineCount();

            try {
                RDFParser.create()
                         .lang(lang)
                         .source(Channels.newInputStream(tracking))
                         .parse(countingSink);
                // finished – no more input
                return;
            } catch (RiotException ex) {
                // Determine how many lines were consumed by Jena before bailing
//                long linesSeenAfter = tracking.getLineCount();
//                long emitted = countingSink.getTupleCount();

//                long badRelativeIdx = emitted;              // 0‑based
//                long badLineNo = linesSeenBefore + badRelativeIdx + 1;

                // Discard the lines that produced triples/quads
//                for (long i = 0; i < emitted && tracking.hasQueuedLines(); i++)
//                    tracking.pollLine();

                // First remaining queued line is invalid – report it
                // String badLine = null; tracking.pollLine();
                // errorCallback.accept(badLineNo, badLine);

                // Skip any buffered remainder until after the next newline so
                // the next parser starts on a clean boundary.
                tracking.skipUntilAfterNextNonBlankLineStart();
            }
        }
    }

    /** Count and Confirm */
    private static final class CountingStreamRDF
        extends StreamRDFWrapper
    {
        private NewlineTrackingChannel lineTracker;

        private long tuples = 0;

        CountingStreamRDF(StreamRDF delegate, NewlineTrackingChannel lineTracker) {
            super(delegate);
            this.lineTracker = lineTracker;
        }

        long getTupleCount()               { return tuples; }

        @Override public void triple(Triple t)  { confirmData(); super.triple(t); }
        @Override public void quad(Quad q){ confirmData(); super.quad(q);  }
        @Override public void base(String base)                       { confirmData(); super.base(base); }
        @Override public void prefix(String p, String uri)            { confirmData(); super.prefix(p, uri); }
        @Override public void start()                                 { confirmData(); super.start(); }
        @Override public void finish()                                { confirmData(); super.finish(); }

        private void confirmData() {
            lineTracker.confirmData();
        }
    }

    /* ================================================================== */
    /**
     * ReadableByteChannel wrapper that
     *   1. counts newline characters, and
     *   2. keeps a queue of completely‑read lines so that the wrapper can map
     *      Jena’s emitted triples/quads back to the corresponding source lines.
     */
    private static final class NewlineTrackingChannel implements ReadableByteChannel {

        private static final int DEFAULT_READ_SIZE = 8192;

        private final ReadableByteChannel delegate;
        private final RingBufferForBytes replayBuffer = new RingBufferForBytes(DEFAULT_READ_SIZE);

        // private ByteBuffer tmp; // A cached byte buffer view over the ring buffer.


        // Current line info
        // private boolean currentLine = false;
        private long nextServeAbsOffset = 0;

        // The last byte served to the client. The buffer may contain yet unserved bytes.
        private long maxServeAbsOffset = 0;

        /**
         * The absolute offset of the first byte of the replay buffer.
         * Whenever a line is committed, this offset is increased by length of the
         * first line in that buffer.
         */
        private long replayBufferAbsStartPos = 0; // end pos implied from the length of the data


        // private long confirmedLineOffset = 0; // This pointer lags behind nextReadOffset

        private long currentLineNumber = 0;
        private long currentLineAbsOffset = 0;
        private boolean currentLineIsInComment = false; // Lines where a '#' follows zero or more white spaces is a comment line.
        private boolean currentLineHasData = false;
        private boolean currentLineConfirmed = false;

        static class LineInfo {
            long absOffset;
            boolean hasData;
            int length;
            boolean isConfirmed;
            public LineInfo(long absOffset, boolean hasData, int length) {
                this(absOffset, hasData, length, false);
            }

            public LineInfo(long absOffset, boolean hasData, int length, boolean isConfirmed) {
                super();
                this.absOffset = absOffset;
                this.hasData = hasData;
                this.length = length;
                this.isConfirmed = isConfirmed;
            }

            public long absOffset() {
                return absOffset;
            }

            public boolean hasData() {
                return hasData;
            }

            public int length() {
                return length;
            }
        }

        private Deque<LineInfo> completedLines = new ArrayDeque<>();

        public NewlineTrackingChannel(ReadableByteChannel delegate) {
            this.delegate = delegate;
        }

        protected void resizeBuffer(int newSize) {
            replayBuffer.resize(newSize);
            // tmp = ByteBuffer.wrap(replayBuffer.getArray());
        }

        // Set the read pointer of the channel to the start of the replay buffer.
        void reset() {
            this.nextServeAbsOffset = replayBufferAbsStartPos;
        }

        // Read more data from the backend and appends it to the buffer.
        // If the buffer has no more capacity, its capacity is increased.
        // Returns the number of bytes read. A return value of 0 means that the backend
        // was consumed.
        protected int fill() {
            // nextReadOffset == endOfReplayOffset
           // Write first to dst or the buffer? If dst is an array, then write to it first.
           int capacity = replayBuffer.capacity();
           if (capacity == 0) {
               long before = replayBuffer.length();
               // double _1_dividedBy_log2 = 1 / Math.log(2);
               // Compute how often we have to double the size in order to fit n additional bytes.
               // Usually this yields 1.
               // int power = (int)Math.ceil(Math.log((before + n) / before) * _1_dividedBy_log2);
               // int after = Ints.saturatedCast(before << power);
               int after = Ints.saturatedCast(before << 1);
               if (before == after) {
                   throw new RuntimeException("Buffer has reached max size");
               }
               resizeBuffer(after);
           }

           // int endPos = replayBuffer.available();
           // Append data to the replay buffer
           int amountRead = replayBuffer.append(DEFAULT_READ_SIZE, bb -> {
               try {
                   int r = delegate.read(bb);
                   return r;
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           });

           return amountRead;
        }

        protected int processNextLineFromBuffer(int maxLen) {
            int result;
            if (nextServeAbsOffset < maxServeAbsOffset) {
                // Find the line closest to the nextServeAbsOffset
                // int l = Math.min(maxLen, Ints.checkedCast(maxServeAbsOffset - nextServeAbsOffset));

                LineInfo bestMatch = null;
                for (LineInfo li : completedLines) {
                    long offset = li.absOffset();
                    if (offset <= nextServeAbsOffset) {
                        bestMatch = li;
                    } else {
                        break;
                    }
                }

                Objects.requireNonNull(bestMatch, "Unexpectely no matching line found");

                long lineEndAbsPos = bestMatch.absOffset() + bestMatch.length();
                int remaining = Ints.checkedCast(lineEndAbsPos - nextServeAbsOffset);
                result = Math.min(maxLen, remaining);

                // int readOffset = getReadOffset();
                //replayBuffer.shallowClone(readOffset, l);

                // result = serveAlreadyReadLine(l);
            } else {
                result = serveNonReadLine(maxLen);
            }
            return result;
        }

        // ISSUE Only process *NEW* data - not data we have already seen.
        // This method stops upon encountering a newline char.
        /**
         * @param maxLen
         * @return Number of bytes read until returning.
         */
        protected int serveNonReadLine(int maxLen) {
            int readOffset = getReadOffset();

            int avail = replayBuffer.available();
            int limit = Math.min(avail, maxLen);

            long absPos = nextServeAbsOffset; //replayBufferAbsStartPos;
            // Serve data from the buffer - stop at the next newline char.
            int i;
            for (i = readOffset; i < readOffset + limit; ++i) {
                // ++nextReadAbsOffset;
                ++absPos;
                int c = replayBuffer.get(i);
                if (c == '\n') {
                    boolean serveOnNewline = true;
                    if (serveOnNewline) {
                        if (i == readOffset) {
                            completeLine(absPos);
                            // we must serve at least 1 byte
                            ++i;
                        } else {
                            // serve the newline with the next read.
                        }
                        break;
                    }
                } else if (c == '#') {
                    if (!currentLineHasData) {
                        currentLineIsInComment = true;
                    }
                } else if (c != ' ' && c != '\t' && c != '\r') {
                    if (!currentLineIsInComment) {
                        currentLineHasData = true;
                    }
                }
            }

            int n = i - readOffset;
            return n;
        }

//        protected int serveAlreadyReadLine(int maxLen) {
//            int readOffset = getReadOffset();
//
//            int avail = replayBuffer.available();
//            int limit = Math.min(avail, maxLen);
//
//            long absPos = replayBufferAbsStartPos;
//            // Serve data from the buffer - stop at the next newline char.
//            int i;
//            for (i = readOffset; i < readOffset + limit; ++i) {
//                // ++nextReadAbsOffset;
//                ++absPos;
//                int c = replayBuffer.get(i);
//                if (c == '\n') {
//                    boolean serveOnNewline = true;
//                    if (serveOnNewline) {
//                        ++i;
//                        break;
//                    }
//                }
//            }
//            int n = i - readOffset;
//            return n;
//        }

        private void completeLine(long absPos) {
            int length = Ints.checkedCast(absPos - currentLineAbsOffset);
            LineInfo lineInfo = new LineInfo(currentLineAbsOffset, currentLineHasData, length);
            currentLineAbsOffset = absPos; // + 1?
            currentLineIsInComment = false;
            currentLineHasData = false;
            completedLines.addLast(lineInfo);
        }

        protected int getReadOffset() {
            return getReadOffset(nextServeAbsOffset);
        }

        protected int getReadOffset(long absPos) {
            int readOffset = Ints.checkedCast(absPos - replayBufferAbsStartPos);

            if (readOffset < 0) {
                throw new IllegalStateException("Read offset is before the replay buffer");
            }

            return readOffset;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int result;
            if (!isOpen()) {
                result = -1;
            } else {
                int bufferReadStart = getReadOffset();
                int bufferReadEnd = replayBuffer.available();

                // Buffer is empty; fill it.
                if (bufferReadStart == bufferReadEnd) {
                    // Append data to the replay buffer
                    fill();
                    bufferReadEnd = replayBuffer.available();
                }

                int dstRemaining = dst.remaining();
                int maxReadAmount = Math.min(bufferReadEnd - bufferReadStart, dstRemaining);
                int i = processNextLineFromBuffer(maxReadAmount);

                nextServeAbsOffset += i;

                int limitBak = dst.limit();
                dst.limit(dst.position() + maxReadAmount);
                // FIXME read consumes data from the buffer
                int actualReadAmount = replayBuffer
                    .shallowClone()
                    .skip(bufferReadStart)
                    .read(dst);
                // int actualReadAmount = replayBuffer.transfer(bufferReadStart, dst);
                // int actualReadAmount = replayBuffer.read(dst);
                dst.limit(limitBak);
                result = actualReadAmount;
            }

            return result;
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        /**
         * Removes blank lines,
         * removes the corresponding bytes from the replay buffer.
         */
        protected void removeBlankLines() {
            Iterator<LineInfo> it = completedLines.iterator();
            LineInfo lineInfo = null;
            while (it.hasNext()) {
                lineInfo = it.next();
                if (lineInfo.hasData()) {
                    break;
                }
                it.remove();
                int l = lineInfo.length;
                replayBufferAbsStartPos += l;
                replayBuffer.skip(l);
            }
        }

        /**
         * A non-blank line is only committed if there is a confirmed successor line.
         *
         * Confirm indicates that a non-blank line produced data,
         * BUT it does not mean that the whole line could be parsed.
         * So we always need to save the last 2 confirmed lines.
         */
        public void confirmData() {
            removeBlankLines();
            long nonBlankLineCount = completedLines.stream().filter(x -> x.hasData()).count();
            if (nonBlankLineCount > 1) {
                LineInfo li = completedLines.removeFirst();
                removeBlankLines();

//                long nextOffset = completedLines.isEmpty()
//                        ? currentLineAbsOffset
//                        : completedLines.getFirst().absOffset();
                long nextOffset = completedLines.getFirst().absOffset();

                int delta = Ints.checkedCast(nextOffset - replayBufferAbsStartPos);
                replayBufferAbsStartPos += delta;
                replayBuffer.skip(delta);
            } else if (nonBlankLineCount == 1) {
                completedLines.getFirst().isConfirmed = true;
            } else {
                // The current line resulted in data but the line end was not yet reached.
                // Garbage data at the end of the line may still cause an error.
                // E.g: <s> <p> <o> . GARBAGE \n
                currentLineConfirmed = true;
            }
//
//            long oldReplayBufferAbsOffset = replayBufferAbsOffset;
//            if (completedLines.isEmpty()) {
//                replayBufferAbsOffset = currentLineAbsOffset;
//            } else {
//                replayBufferAbsOffset = completedLines.getFirst().absOffset();
//            }
//
//            int delta = Ints.checkedCast(replayBufferAbsOffset - oldReplayBufferAbsOffset);
//            replayBuffer.skip(delta);
        }


        public RingBufferForBytes extractFirstLine() {
            removeBlankLines();

            RingBufferForBytes result;

            if (completedLines.isEmpty()) {
                // finish reading the current line.
                finishReadingCurrentLine();
            }

            LineInfo li = completedLines.getFirst();
            long linePos = li.absOffset();
            int start = getReadOffset(linePos);
            int len = li.length();

            result = replayBuffer.shallowClone(start, len);

            return result;
        }


        // long getLineCount()            { return lineCount; }
        // boolean hasQueuedLines()       { return !lines.isEmpty(); }
        // String pollLine()              { return lines.pollFirst(); }

        /**
         * Reads and discards bytes until (and including) the next '\n'.  Used to
         * skip the rest of a malformed line that was partially read into Jena’s
         * internal buffer at the time it threw the error.
         */
        void skipUntilAfterNextNonBlankLineStart() throws IOException {
            removeBlankLines();
            if (!completedLines.isEmpty()) {
                // Reset to the last non-blank line.
                long absOffset = completedLines.getFirst().absOffset();
                nextServeAbsOffset = absOffset;
            }

            finishReadingCurrentLine();

            // May have reached end of data
            if (!completedLines.isEmpty()) {
                LineInfo lineInfo = completedLines.getFirst();
                long nextOffset = lineInfo.absOffset();
                int delta = Ints.checkedCast(nextOffset - replayBufferAbsStartPos);
                replayBufferAbsStartPos += delta;
                replayBuffer.skip(delta);
            } else {
                // Reached end of data skip the last line
                replayBuffer.skip(replayBuffer.available());
            }
        }

        private void finishReadingCurrentLine() {
            // Make sure that we read one line with data - which we consider
            // to be the offending line.
            while (true) {
                processNextLineFromBuffer(DEFAULT_READ_SIZE);
                removeBlankLines(); // Immediately remove a blank line again.
                if (!completedLines.isEmpty()) {
                    break;
                }
                int fillAmount = fill();
                if (fillAmount <= 0) {
                    completeLine(nextServeAbsOffset);
                    break;
                }
            }
        }
    }
}
