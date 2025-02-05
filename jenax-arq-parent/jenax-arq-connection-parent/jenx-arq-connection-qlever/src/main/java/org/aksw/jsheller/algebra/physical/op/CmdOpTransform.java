package org.aksw.jsheller.algebra.physical.op;

import java.util.List;

public interface CmdOpTransform
{
    CmdOp transform(CmdOpPipe op, CmdOp subOp1, CmdOp subOp2);
    CmdOp transform(CmdOpGroup op, List<CmdOp> subOps);
    CmdOp transform(CmdOpSubst op, CmdOp subOp);
    CmdOp transform(CmdOpExec op, List<CmdOp> subOps);
    CmdOp transform(CmdOpToArg op, CmdOp subOp);
    CmdOp transform(CmdOpString op);
    CmdOp transform(CmdOpFile op);
}
