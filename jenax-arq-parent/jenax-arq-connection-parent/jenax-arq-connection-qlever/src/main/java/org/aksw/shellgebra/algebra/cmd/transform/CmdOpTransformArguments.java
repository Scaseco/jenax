package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.List;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpFile;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpString;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.exec.SysRuntime;

public class CmdOpTransformArguments
    extends CmdOpTransformBase
{
    protected SysRuntime runtime;

    public CmdOpTransformArguments(SysRuntime runtime) {
        super();
        this.runtime = runtime;
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdOp> subOps) {
        CmdOp result;
        if (subOps.stream().anyMatch(x -> x instanceof CmdOpFile)) {
            List<CmdOp> newArgs = subOps.stream().map(this::handleFile).toList();
            result = new CmdOpExec(op.getName(), newArgs);
        } else {
            result = super.transform(op, subOps);
        }
        return result;
    }

    protected CmdOp handleFile(CmdOp cmdOp) {
        CmdOp result = cmdOp instanceof CmdOpFile cf
            ? new CmdOpString(runtime.quoteFileArgument(cf.getPath()))
            : cmdOp;
        return result;
    }

}
