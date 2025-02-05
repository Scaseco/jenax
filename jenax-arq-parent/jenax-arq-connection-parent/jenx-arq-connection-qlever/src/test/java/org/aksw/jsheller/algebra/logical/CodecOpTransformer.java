package org.aksw.jsheller.algebra.logical;

public class CodecOpTransformer {
    public static CodecOp transform(CodecOp op, CodecOpTransform transform) {
        CodecOpVisitorApplyTransform visitor = new CodecOpVisitorApplyTransform(transform);
        CodecOp result = op.accept(visitor);
        return result;
    }
}
