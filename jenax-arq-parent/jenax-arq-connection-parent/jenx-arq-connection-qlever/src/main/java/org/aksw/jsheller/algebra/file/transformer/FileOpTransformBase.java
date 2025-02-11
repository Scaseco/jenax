package org.aksw.jsheller.algebra.file.transformer;

import org.aksw.jsheller.algebra.file.op.FileOp;
import org.aksw.jsheller.algebra.file.op.FileOpName;
import org.aksw.jsheller.algebra.file.op.FileOpTranscode;
import org.aksw.jsheller.algebra.file.op.FileOpVar;

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
}
