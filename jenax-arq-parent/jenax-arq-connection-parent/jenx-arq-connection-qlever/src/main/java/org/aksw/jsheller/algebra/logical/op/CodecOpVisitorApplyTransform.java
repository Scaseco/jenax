package org.aksw.jsheller.algebra.logical.op;

import java.util.List;
import java.util.Objects;

public class CodecOpVisitorApplyTransform
    implements CodecOpVisitor<CodecOp>
{
    protected CodecOpTransform transform;

    public CodecOpVisitorApplyTransform(CodecOpTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public CodecOp visit(CodecOpFile op) {
        CodecOp result = transform.transform(op);
        return result;
    }

    @Override
    public CodecOp visit(CodecOpCodecName op) {
        CodecOp subOp = op.getSubOp();
        CodecOp newSubOp = subOp.accept(this);
        CodecOp result = transform.transform(op, newSubOp);
        return result;
    }

    @Override
    public CodecOp visit(CodecOpConcat op) {
        List<CodecOp> newSubOps = op.getSubOps().stream().map(subOp -> subOp.accept(this)).toList();
        CodecOp result = transform.transform(op, newSubOps);
        return result;
    }

    @Override
    public CodecOp visit(CodecOpCommand op) {
        CodecOp result = transform.transform(op);
        return result;
    }

    /*
    @Override
    public CodecOp visit(CodecOpCommandGroup op) {
        List<CodecOp> newSubOps = op.getSubOps().stream().map(subOp -> subOp.accept(this)).toList();
        CodecOp result = transform.transform(op, newSubOps);
        return result;
    }

    @Override
    public CodecOp visit(CodecOpPipe op) {
        CodecOp newSubOp1 = op.getSubOp1().accept(this);
        CodecOp newSubOp2 = op.getSubOp2().accept(this);
        CodecOp result = transform.transform(op, newSubOp1, newSubOp2);
        return result;
    }
    */
}
