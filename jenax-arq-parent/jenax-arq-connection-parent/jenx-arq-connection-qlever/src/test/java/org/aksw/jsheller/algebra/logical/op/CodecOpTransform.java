package org.aksw.jsheller.algebra.logical.op;

import java.util.List;

public interface CodecOpTransform {
    CodecOp transform(CodecOpFile op);
    CodecOp transform(CodecOpCodecName op, CodecOp subOp);
    CodecOp transform(CodecOpConcat op, List<CodecOp> subOps);
    CodecOp transform(CodecOpCommand op);
    // CodecOp transform(CodecOpCommandGroup op, List<CodecOp> subOps);
    // CodecOp transform(CodecOpPipe op, CodecOp subOp1, CodecOp subOp2);
}
