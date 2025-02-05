package org.aksw.jsheller.algebra.logical;

import java.util.List;

public class CodecOpTransformBase
    implements CodecOpTransform {

    // TODO Avoid copy if subOps have not changed

    @Override
    public CodecOp transform(CodecOpFile op) {
        return op;
    }

    @Override
    public CodecOp transform(CodecOpCodecName op, CodecOp subOp) {
        return new CodecOpCodecName(op.getName(), subOp);
    }

    @Override
    public CodecOp transform(CodecOpConcat op, List<CodecOp> subOps) {
        return new CodecOpConcat(subOps);
    }

    @Override
    public CodecOp transform(CodecOpCommand op) {
        return op;
        // return new CodecOpCommand(op.getCmdArray());
    }

    /*
    @Override
    public CodecOp transform(CodecOpCommandGroup op, List<CodecOp> subOps) {
        return CodecOpCommandGroup.of(subOps);
    }

    @Override
    public CodecOp transform(CodecOpPipe op, CodecOp subOp1, CodecOp subOp2) {
        return new CodecOpPipe(subOp1, subOp2);
    }
    */
}
