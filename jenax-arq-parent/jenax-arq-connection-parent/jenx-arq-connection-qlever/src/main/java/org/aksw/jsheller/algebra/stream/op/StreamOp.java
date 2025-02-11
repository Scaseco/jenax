package org.aksw.jsheller.algebra.stream.op;

public interface StreamOp {
    <T> T accept(StreamOpVisitor<T> visitor);
}
