package org.aksw.jsheller.algebra.stream.transformer;

import java.util.List;

import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;

public interface StreamOpTransform {
    StreamOp transform(StreamOpFile op);
    StreamOp transform(StreamOpTranscode op, StreamOp subOp);
    StreamOp transform(StreamOpConcat op, List<StreamOp> subOps);
    StreamOp transform(StreamOpCommand op);
    StreamOp transform(StreamOpVar op);
    // CodecOp transform(CodecOpCommandGroup op, List<CodecOp> subOps);
    // CodecOp transform(CodecOpPipe op, CodecOp subOp1, CodecOp subOp2);
}
