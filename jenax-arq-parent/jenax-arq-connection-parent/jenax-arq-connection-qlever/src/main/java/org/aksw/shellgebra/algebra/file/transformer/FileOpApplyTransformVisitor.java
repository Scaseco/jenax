package org.aksw.shellgebra.algebra.file.transformer;

import org.aksw.shellgebra.algebra.file.op.FileOp;
import org.aksw.shellgebra.algebra.file.op.FileOpName;
import org.aksw.shellgebra.algebra.file.op.FileOpOverStreamOp;
import org.aksw.shellgebra.algebra.file.op.FileOpTranscode;
import org.aksw.shellgebra.algebra.file.op.FileOpVar;
import org.aksw.shellgebra.algebra.file.op.FileOpVisitor;

public class FileOpApplyTransformVisitor
    implements FileOpVisitor<FileOp>
{
    protected FileOpTransform transform;

    public FileOpApplyTransformVisitor(FileOpTransform transform) {
        super();
        this.transform = transform;
    }

    @Override
    public FileOp visit(FileOpName op) {
        FileOp result = transform.transform(op);
        return result;
    }

    @Override
    public FileOp visit(FileOpTranscode op) {
        FileOp oldOp = op.getSubOp();
        FileOp newOp = oldOp.accept(this);
        FileOp result = transform.transform(op, newOp);
        return result;
    }

    @Override
    public FileOp visit(FileOpVar op) {
        FileOp result = transform.transform(op);
        return result;
    }

    @Override
    public FileOp visit(FileOpOverStreamOp op) {
        FileOp result = transform.transform(op);
        return result;
    }
}
