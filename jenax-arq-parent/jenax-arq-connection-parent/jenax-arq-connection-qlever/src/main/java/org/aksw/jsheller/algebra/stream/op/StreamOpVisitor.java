package org.aksw.jsheller.algebra.stream.op;

public interface StreamOpVisitor<T> {
    T visit(StreamOpFile op);
    T visit(StreamOpTranscode op);
    T visit(StreamOpConcat op);
    T visit(StreamOpCommand op);
    T visit(StreamOpVar op);
}
