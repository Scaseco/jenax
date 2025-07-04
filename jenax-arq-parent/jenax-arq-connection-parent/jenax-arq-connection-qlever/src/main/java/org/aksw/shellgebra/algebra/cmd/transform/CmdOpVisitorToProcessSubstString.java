package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpFile;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipe;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpRedirect;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpString;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpSubst;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.exec.CmdStrOps;

public class CmdOpVisitorToProcessSubstString
    implements CmdOpVisitor<String>
{
    protected CmdStrOps strOps;

    public CmdOpVisitorToProcessSubstString(CmdStrOps strOps) {
        super();
        this.strOps = Objects.requireNonNull(strOps);
    }

    @Override
    public String visit(CmdOpExec op) {
        List<CmdOp> subOps = op.getSubOps();
        List<String> argStrs = CmdOpTransformLib.transformAll(this, subOps);
        String result = strOps.call(op.getName(), argStrs);
        return result;
    }

    @Override
    public String visit(CmdOpPipe op) {
        String before = op.getSubOp1().accept(this);
        String after = op.getSubOp2().accept(this);
        String result = strOps.pipe(before, after);
        return result;
    }

    @Override
    public String visit(CmdOpGroup op) {
        List<String> strs = CmdOpTransformLib.transformAll(this, op.getSubOps());
        String result = strOps.group(strs);
        return result;
    }

    @Override
    public String visit(CmdOpSubst op) {
        String str = op.getSubOp().accept(this);
        String result = strOps.subst(str);
        return result;
    }

    @Override
    public String visit(CmdOpString op) {
        return op.getValue();
    }

    @Override
    public String visit(CmdOpToArg op) {
        String str = op.getSubOp().accept(this);
        String result = strOps.quoteArg(str);
        return result;
    }

    /** For proper stringification file nodes of exec nodes need to be replaced with strings.
     *  See {@link CmdOpTransformArguments}
     */
    @Override
    public String visit(CmdOpFile op) {
        String str = op.getPath(); // op.getSubOp().accept(this);
        String result = strOps.quoteArg(str);
        return result;
    }

    @Override
    public String visit(CmdOpRedirect op) {
        String cmdStr = op.getSubOp().accept(this);
        String fileName = op.getFileName();
        String result = strOps.redirect(cmdStr, fileName);
        return result;
    }
}
