package org.aksw.jsheller.algebra.physical;

public class CmdOpTransformer {
    public static CmdOp transform(CmdOp op, CmdOpTransform transform) {
        CmdOpApplyTransformVisitor visitor = new CmdOpApplyTransformVisitor(transform);
        CmdOp result = op.accept(visitor);
        return result;
    }
}
