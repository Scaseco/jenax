package org.aksw.jsheller.algebra.file.transformer;

import org.aksw.jsheller.algebra.file.op.FileOp;
import org.aksw.jsheller.algebra.file.op.FileOpOverStreamOp;
import org.aksw.jsheller.algebra.file.op.FileOpName;
import org.aksw.jsheller.algebra.file.op.FileOpTranscode;
import org.aksw.jsheller.algebra.file.op.FileOpVar;

public interface FileOpTransform
{
    FileOp transform(FileOpName op);
    FileOp transform(FileOpTranscode op, FileOp subOp);
    FileOp transform(FileOpVar op);
    FileOp transform(FileOpOverStreamOp op);
}
