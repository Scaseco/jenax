package org.aksw.jsheller.algebra.logical;

public interface CodecOpVisitor<T> {
    T visit(CodecOpFile op);
    T visit(CodecOpCodecName op);
    T visit(CodecOpConcat op);
    // T visit(CodecOpCommandGroup op);
    T visit(CodecOpCommand op);
    // T visit(CodecOpPipe op);
}
