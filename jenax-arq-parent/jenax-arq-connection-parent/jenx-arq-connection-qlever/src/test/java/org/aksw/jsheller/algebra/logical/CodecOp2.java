package org.aksw.jsheller.algebra.logical;

import java.util.Objects;

public abstract class CodecOp2
    extends CodecOpBase
{
    protected CodecOp subOp1;
    protected CodecOp subOp2;

    public CodecOp2(CodecOp subOp1, CodecOp subOp2) {
        super();
        this.subOp1 = Objects.requireNonNull(subOp1);
        this.subOp2 = Objects.requireNonNull(subOp2);
    }

    public CodecOp getSubOp1() {
        return subOp1;
    }

    public CodecOp getSubOp2() {
        return subOp2;
    }
}
