package org.aksw.shellgebra.algebra.file.op;

import org.aksw.shellgebra.algebra.stream.op.StreamOp;

/**
 * Operator to write a stream to a file.
 * Typically a redirect:
 * <pre>
 * command arg > file.out
 * </pre>
 */
public class FileOpOverStreamOp
    extends FileOp0
{
    protected String destFilename;
    protected StreamOp streamOp;

    public FileOpOverStreamOp(String destFilename, StreamOp streamOp) {
        super();
        this.destFilename = destFilename;
        this.streamOp = streamOp;
    }

    public String getDestFilename() {
        return destFilename;
    }

    public StreamOp getStreamOp() {
        return streamOp;
    }

    @Override
    public <T> T accept(FileOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
