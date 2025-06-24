package org.aksw.shellgebra.algebra.stream.op;

public interface StreamOp
    extends HasStreamOp
{
    <T> T accept(StreamOpVisitor<T> visitor);

    @Override
    default StreamOp getStreamOp() {
        return this;
    }
}
