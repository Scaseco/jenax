package org.aksw.jsheller.algebra.stream.transformer;

import org.aksw.jsheller.algebra.stream.op.HasStreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOp;

public class StreamOpTransformer {
    public static <T extends HasStreamOp> T transform(StreamOp op, StreamOpTransformGeneric<T> transform) {
        StreamOpVisitorApplyTransform<T> visitor = new StreamOpVisitorApplyTransform<>(transform);
        T result = op.accept(visitor);
        return result;
    }

//    public static <T extends HasStreamOp> T transformGeneric(StreamOp op, StreamOpTransformGeneric<T> transform) {
//        StreamOpTransformGeneric<T> visitor = new StreamOpVisitorApplyTransform(transform);
//        T result = op.accept(visitor);
//        return result;
//    }

}
