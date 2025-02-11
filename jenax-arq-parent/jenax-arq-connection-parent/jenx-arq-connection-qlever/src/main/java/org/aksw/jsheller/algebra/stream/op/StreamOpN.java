package org.aksw.jsheller.algebra.stream.op;

import java.util.List;

public abstract class StreamOpN
    implements StreamOp
{
    // public record Arg(boolean isOp, CodecOp subOp, String arg) {}

    protected List<StreamOp> subOps;

    public StreamOpN(List<StreamOp> subOps) {
        super();
        this.subOps = List.copyOf(subOps);
    }

    public List<StreamOp> getSubOps() {
        return subOps;
    }
}
