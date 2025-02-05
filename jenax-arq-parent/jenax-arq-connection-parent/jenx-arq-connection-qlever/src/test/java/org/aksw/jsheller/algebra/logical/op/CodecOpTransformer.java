package org.aksw.jsheller.algebra.logical.op;

public class CodecOpTransformer {
    public static CodecOp transform(CodecOp op, CodecOpTransform transform) {
        CodecOpVisitorApplyTransform visitor = new CodecOpVisitorApplyTransform(transform);
        CodecOp result = op.accept(visitor);
        return result;
    }
}
