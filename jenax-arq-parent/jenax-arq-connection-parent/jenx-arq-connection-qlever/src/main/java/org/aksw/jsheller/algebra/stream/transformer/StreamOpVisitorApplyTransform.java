package org.aksw.jsheller.algebra.stream.transformer;

import java.util.List;
import java.util.Objects;

import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;
import org.aksw.jsheller.algebra.stream.op.StreamOpVisitor;

public class StreamOpVisitorApplyTransform
    implements StreamOpVisitor<StreamOp>
{
    protected StreamOpTransform transform;

    public StreamOpVisitorApplyTransform(StreamOpTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public StreamOp visit(StreamOpFile op) {
        StreamOp result = transform.transform(op);
        return result;
    }

    @Override
    public StreamOp visit(StreamOpTranscode op) {
        StreamOp subOp = op.getSubOp();
        StreamOp newSubOp = subOp.accept(this);
        StreamOp result = transform.transform(op, newSubOp);
        return result;
    }

    @Override
    public StreamOp visit(StreamOpConcat op) {
        List<StreamOp> newSubOps = op.getSubOps().stream().map(subOp -> subOp.accept(this)).toList();
        StreamOp result = transform.transform(op, newSubOps);
        return result;
    }

    @Override
    public StreamOp visit(StreamOpCommand op) {
        StreamOp result = transform.transform(op);
        return result;
    }

    @Override
    public StreamOp visit(StreamOpVar op) {
        StreamOp result = transform.transform(op);
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
