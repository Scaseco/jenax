package org.aksw.jsheller.algebra.stream.transformer;

import java.util.List;

import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;

public class StreamOpTransformBase
    implements StreamOpTransform {

    // TODO Avoid copy if subOps have not changed

    @Override
    public StreamOp transform(StreamOpFile op) {
        return op;
    }

    @Override
    public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
        return new StreamOpTranscode(op.getName(), op.getTranscodeMode(), subOp);
    }

    @Override
    public StreamOp transform(StreamOpConcat op, List<StreamOp> subOps) {
        return new StreamOpConcat(subOps);
    }

    @Override
    public StreamOp transform(StreamOpCommand op) {
        return op;
    }

    @Override
    public StreamOp transform(StreamOpVar op) {
        return op;
    }
}
