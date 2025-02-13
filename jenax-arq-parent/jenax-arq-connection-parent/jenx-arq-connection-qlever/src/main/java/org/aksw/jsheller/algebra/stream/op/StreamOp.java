package org.aksw.jsheller.algebra.stream.op;

public interface StreamOp
    extends HasStreamOp
{
    <T> T accept(StreamOpVisitor<T> visitor);

    @Override
    default StreamOp getStreamOp() {
        return this;
    }
}
