package org.aksw.jsheller.algebra.logical;

import java.util.Objects;

public abstract class CodecOp1
    extends CodecOpBase
{
    protected CodecOp subOp;

    public CodecOp1(CodecOp subOp) {
        super();
        this.subOp = Objects.requireNonNull(subOp);
    }

    public CodecOp getSubOp() {
        return subOp;
    }
}
