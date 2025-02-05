package org.aksw.jsheller.algebra.logical;

import java.util.List;

public abstract class CodecOpN
    implements CodecOp
{
    // public record Arg(boolean isOp, CodecOp subOp, String arg) {}

    protected List<CodecOp> subOps;

    public CodecOpN(List<CodecOp> subOps) {
        super();
        this.subOps = List.copyOf(subOps);
    }

    public List<CodecOp> getSubOps() {
        return subOps;
    }
}
