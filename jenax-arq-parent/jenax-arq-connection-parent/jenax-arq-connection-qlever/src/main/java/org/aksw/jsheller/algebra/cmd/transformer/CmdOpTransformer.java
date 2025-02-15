package org.aksw.jsheller.algebra.cmd.transformer;

import org.aksw.jsheller.algebra.cmd.op.CmdOp;

public class CmdOpTransformer {
    public static CmdOp transform(CmdOp op, CmdOpTransform transform) {
        CmdOpApplyTransformVisitor visitor = new CmdOpApplyTransformVisitor(transform);
        CmdOp result = op.accept(visitor);
        return result;
    }
}
