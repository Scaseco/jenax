package org.aksw.jsheller.algebra.logical.op;

public interface CodecOp {
    <T> T accept(CodecOpVisitor<T> visitor);
}
