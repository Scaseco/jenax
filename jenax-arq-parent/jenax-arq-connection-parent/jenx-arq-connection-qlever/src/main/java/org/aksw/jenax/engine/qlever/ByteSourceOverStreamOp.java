package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpVisitor;
import org.aksw.jsheller.algebra.stream.transform.StreamOpVisitorStream;

import com.google.common.io.ByteSource;

/**
 * Byte source over a logical plan.
 */
public class ByteSourceOverStreamOp
    extends ByteSource
{
    protected StreamOp op;
    protected StreamOpVisitor<InputStream> streamVisitor;

    public ByteSourceOverStreamOp(StreamOp op) {
        this(op, StreamOpVisitorStream.getSingleton());
    }

    public ByteSourceOverStreamOp(StreamOp op, StreamOpVisitor<InputStream> streamVisitor) {
        this.op = Objects.requireNonNull(op);
        this.streamVisitor = Objects.requireNonNull(streamVisitor);
    }

    @Override
    public InputStream openStream() throws IOException {
        return op.accept(streamVisitor);
    }
}
