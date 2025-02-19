package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpFile;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipe;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpRedirect;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpSubst;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;

public interface CmdOpTransform
{
    CmdOp transform(CmdOpPipe op, CmdOp subOp1, CmdOp subOp2);
    CmdOp transform(CmdOpGroup op, List<CmdOp> subOps);
    CmdOp transform(CmdOpSubst op, CmdOp subOp);
    CmdOp transform(CmdOpExec op, List<CmdOp> subOps);
    CmdOp transform(CmdOpToArg op, CmdOp subOp);
    CmdOp transform(CmdOpString op);
    CmdOp transform(CmdOpFile op);
    CmdOp transform(CmdOpRedirect op, CmdOp subOp);
}
