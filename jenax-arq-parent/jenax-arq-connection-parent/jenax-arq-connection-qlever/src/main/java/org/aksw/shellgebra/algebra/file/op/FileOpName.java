package org.aksw.shellgebra.algebra.file.op;

public class FileOpName
    extends FileOp0
{
    protected final String fileName;

    public FileOpName(String fileName) {
        super();
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public <T> T accept(FileOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
