package org.aksw.jsheller.algebra.physical.op;

import java.util.ArrayList;
import java.util.List;

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
}
