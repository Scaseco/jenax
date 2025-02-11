package org.aksw.jsheller.algebra.stream.transformer;

import org.aksw.jsheller.algebra.stream.op.StreamOp;

public class StreamOpTransformer {
    public static StreamOp transform(StreamOp op, StreamOpTransform transform) {
        StreamOpVisitorApplyTransform visitor = new StreamOpVisitorApplyTransform(transform);
        StreamOp result = op.accept(visitor);
        return result;
    }
}
