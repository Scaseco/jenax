package org.aksw.shellgebra.algebra.file.transformer;

import org.aksw.shellgebra.algebra.file.op.FileOp;

public class FileOpTransformer {
    public static FileOp transform(FileOp op, FileOpTransform transform) {
        FileOpApplyTransformVisitor visitor = new FileOpApplyTransformVisitor(transform);
        FileOp result = op.accept(visitor);
        return result;
    }
}
