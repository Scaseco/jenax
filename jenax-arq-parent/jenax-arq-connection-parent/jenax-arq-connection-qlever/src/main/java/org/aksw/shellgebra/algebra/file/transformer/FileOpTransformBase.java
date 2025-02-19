package org.aksw.shellgebra.algebra.file.transformer;

import org.aksw.shellgebra.algebra.file.op.FileOp;
import org.aksw.shellgebra.algebra.file.op.FileOpName;
import org.aksw.shellgebra.algebra.file.op.FileOpOverStreamOp;
import org.aksw.shellgebra.algebra.file.op.FileOpTranscode;
import org.aksw.shellgebra.algebra.file.op.FileOpVar;

public class FileOpTransformBase
    implements FileOpTransform
{
    @Override
    public FileOp transform(FileOpName op) {
        return op;
    }

    @Override
    public FileOp transform(FileOpTranscode op, FileOp subOp) {
        return new FileOpTranscode(op.getCodecName(), op.getTranscodeMode(), subOp);
    }

    @Override
    public FileOp transform(FileOpVar op) {
        return op;
    }

    @Override
    public FileOp transform(FileOpOverStreamOp op) {
        return op;
    }
}
