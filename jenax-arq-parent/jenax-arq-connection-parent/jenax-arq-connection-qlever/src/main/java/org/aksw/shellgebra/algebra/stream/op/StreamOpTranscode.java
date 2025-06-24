package org.aksw.shellgebra.algebra.stream.op;

import java.util.Objects;

import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.common.Transcoding;

/** Encode/Decode the underlying stream with a codec of the given name. */
public class StreamOpTranscode
  extends StreamOp1
{
    protected Transcoding transcoding;

    public StreamOpTranscode(String name, TranscodeMode transcodeMode, StreamOp subOp) {
        this(new Transcoding(name, transcodeMode), subOp);
    }

    public StreamOpTranscode(Transcoding transcoding, StreamOp subOp) {
        super(subOp);
        this.transcoding = Objects.requireNonNull(transcoding);
    }

    public Transcoding getTranscoding() {
        return transcoding;
    }

    public String getName() {
        return transcoding.name();
    }

    public TranscodeMode getTranscodeMode() {
        return transcoding.mode();
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        TranscodeMode mode = getTranscodeMode();
        String s = switch (mode) {
        case ENCODE -> "encode";
        case DECODE -> "decode";
        };

        return "(" + s + " (" + getName() + ") " + subOp + ")";
    }
}
