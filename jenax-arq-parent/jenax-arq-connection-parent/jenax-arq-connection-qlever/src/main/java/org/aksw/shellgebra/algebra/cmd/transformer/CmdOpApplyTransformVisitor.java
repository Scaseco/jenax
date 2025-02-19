package org.aksw.shellgebra.algebra.cmd.transformer;

import java.util.ArrayList;
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
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;

public class CmdOpApplyTransformVisitor
    implements CmdOpVisitor<CmdOp>
{
    protected CmdOpTransform transform;

    public CmdOpApplyTransformVisitor(CmdOpTransform transform) {
        super();
        this.transform = transform;
    }

    public static List<CmdOp> transformAll(CmdOpVisitor<? extends CmdOp> transform, List<? extends CmdOp> subOps) {
        List<CmdOp> newOps = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            CmdOp newOp = subOp.accept(transform);
            newOps.add(newOp);
        }
        return newOps;
    }

    @Override
    public CmdOp visit(CmdOpExec op) {
        List<CmdOp> newOps = transformAll(this, op.getSubOps());
        CmdOp result = transform.transform(op, newOps);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpPipe op) {
//    	CmdOp subOp1 = op.getSubOp1();
//    	CmdOp subOp2 = op.getSubOp2();
        CmdOp newOp1 = op.getSubOp1().accept(this);
        CmdOp newOp2 = op.getSubOp2().accept(this);
        CmdOp result = transform.transform(op, newOp1, newOp2);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpGroup op) {
        List<CmdOp> newOps = transformAll(this, op.getSubOps());
        CmdOp result = transform.transform(op, newOps);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpString op) {
        CmdOp result = transform.transform(op);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpSubst op) {
        CmdOp subOp = op.getSubOp().accept(this);
        CmdOp result = transform.transform(op, subOp);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpToArg op) {
        CmdOp subOp = op.getSubOp().accept(this);
        CmdOp result = transform.transform(op, subOp);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpFile op) {
        CmdOp result = transform.transform(op);
        return result;
    }

    @Override
    public CmdOp visit(CmdOpRedirect op) {
        CmdOp newOp = op.getSubOp().accept(this);
        CmdOp result = transform.transform(op, newOp);
        return result;
    }
}
