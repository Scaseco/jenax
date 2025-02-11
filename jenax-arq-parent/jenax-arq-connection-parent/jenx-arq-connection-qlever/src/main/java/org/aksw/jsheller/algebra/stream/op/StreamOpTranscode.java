package org.aksw.jsheller.algebra.stream.op;

import java.util.Objects;

import org.aksw.jsheller.algebra.common.TranscodeMode;

/** Encode/Decode the underlying stream with a codec of the given name. */
public class StreamOpTranscode
  extends StreamOp1
{
    protected String name;
    protected TranscodeMode transcodeMode;

    public StreamOpTranscode(String name, TranscodeMode transcodeMode, StreamOp subOp) {
        super(subOp);
        this.transcodeMode = Objects.requireNonNull(transcodeMode);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public TranscodeMode getTranscodeMode() {
        return transcodeMode;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(transcode (" + getName() + ") " + subOp + ")";
    }
}
