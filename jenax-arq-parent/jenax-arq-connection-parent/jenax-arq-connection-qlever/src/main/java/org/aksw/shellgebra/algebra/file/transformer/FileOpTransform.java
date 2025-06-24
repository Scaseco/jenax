package org.aksw.shellgebra.algebra.file.transformer;

import org.aksw.shellgebra.algebra.file.op.FileOp;
import org.aksw.shellgebra.algebra.file.op.FileOpName;
import org.aksw.shellgebra.algebra.file.op.FileOpOverStreamOp;
import org.aksw.shellgebra.algebra.file.op.FileOpTranscode;
import org.aksw.shellgebra.algebra.file.op.FileOpVar;

public interface FileOpTransform
{
    FileOp transform(FileOpName op);
    FileOp transform(FileOpTranscode op, FileOp subOp);
    FileOp transform(FileOpVar op);
    FileOp transform(FileOpOverStreamOp op);
}
