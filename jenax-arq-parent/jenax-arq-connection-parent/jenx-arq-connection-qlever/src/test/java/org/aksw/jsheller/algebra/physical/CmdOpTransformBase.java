package org.aksw.jsheller.algebra.physical;

import java.util.List;

public class CmdOpTransformBase
    implements CmdOpTransform
{
    @Override
    public CmdOp transform(CmdOpPipe op, CmdOp subOp1, CmdOp subOp2) {
        return new CmdOpPipe(subOp1, subOp2);
    }

    @Override
    public CmdOp transform(CmdOpGroup op, List<CmdOp> subOps) {
        return new CmdOpGroup(subOps);
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdOp> subOps) {
        return new CmdOpExec(op.getName(), subOps);
    }

    @Override
    public CmdOp transform(CmdOpSubst op, CmdOp subOp) {
        return new CmdOpSubst(subOp);
    }

    @Override
    public CmdOp transform(CmdOpToArg op, CmdOp subOp) {
        return new CmdOpToArg(subOp);
    }

    @Override
    public CmdOp transform(CmdOpString op) {
        return op;
        // return new CmdOpString(op.value);
    }

    @Override
    public CmdOp transform(CmdOpFile op) {
        return op;
        // return new CmdOpString(op.value);
    }
}
