package org.aksw.shellgebra.algebra.cmd.transformer;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;

public class CmdOpTransformer {
    public static CmdOp transform(CmdOp op, CmdOpTransform transform) {
        CmdOpApplyTransformVisitor visitor = new CmdOpApplyTransformVisitor(transform);
        CmdOp result = op.accept(visitor);
        return result;
    }
}
