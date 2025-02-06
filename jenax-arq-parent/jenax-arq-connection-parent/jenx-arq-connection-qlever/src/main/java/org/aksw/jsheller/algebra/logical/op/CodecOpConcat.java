package org.aksw.jsheller.algebra.logical.op;

import java.util.List;
import java.util.stream.Collectors;

public class CodecOpConcat
    extends CodecOpN
{
    protected CodecOpConcat(List<CodecOp> subOps) {
        super(subOps);
    }

    @Override
    public <T> T accept(CodecOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static CodecOp of(List<CodecOp> subOps) {
        CodecOp result = subOps.size() == 1
            ? subOps.get(0)
            : new CodecOpConcat(subOps);
        return result;
    }

    @Override
    public String toString() {
        return "(concat " + getSubOps().stream()
            .map(Object::toString)
            .collect(Collectors.joining(" ")) + ")";
    }
}
