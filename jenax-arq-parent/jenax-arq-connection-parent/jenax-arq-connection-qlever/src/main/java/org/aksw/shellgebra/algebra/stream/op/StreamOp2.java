package org.aksw.shellgebra.algebra.stream.op;

import java.util.Objects;

public abstract class StreamOp2
    extends StreamOpBase
{
    protected StreamOp subOp1;
    protected StreamOp subOp2;

    public StreamOp2(StreamOp subOp1, StreamOp subOp2) {
        super();
        this.subOp1 = Objects.requireNonNull(subOp1);
        this.subOp2 = Objects.requireNonNull(subOp2);
    }

    public StreamOp getSubOp1() {
        return subOp1;
    }

    public StreamOp getSubOp2() {
        return subOp2;
    }
}
