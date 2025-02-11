package org.aksw.jsheller.algebra.stream.op;

import java.util.List;
import java.util.stream.Collectors;

public class StreamOpConcat
    extends StreamOpN
{
    public StreamOpConcat(List<StreamOp> subOps) {
        super(subOps);
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static StreamOp of(List<StreamOp> subOps) {
        StreamOp result = subOps.size() == 1
            ? subOps.get(0)
            : new StreamOpConcat(subOps);
        return result;
    }

    @Override
    public String toString() {
        return "(concat " + getSubOps().stream()
            .map(Object::toString)
            .collect(Collectors.joining(" ")) + ")";
    }
}
