package org.aksw.jsheller.algebra.logical;

public interface CodecOp {
    <T> T accept(CodecOpVisitor<T> visitor);
}
