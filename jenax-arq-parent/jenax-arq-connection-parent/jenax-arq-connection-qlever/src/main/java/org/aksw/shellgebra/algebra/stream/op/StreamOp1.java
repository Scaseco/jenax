package org.aksw.shellgebra.algebra.stream.op;

import java.util.Objects;

public abstract class StreamOp1
    extends StreamOpBase
{
    protected StreamOp subOp;

    public StreamOp1(StreamOp subOp) {
        super();
        this.subOp = Objects.requireNonNull(subOp);
    }

    public StreamOp getSubOp() {
        return subOp;
    }
}
