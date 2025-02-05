package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.aksw.jsheller.algebra.logical.op.CodecOp;
import org.aksw.jsheller.algebra.logical.op.CodecOpVisitor;
import org.aksw.jsheller.algebra.logical.transform.CodecOpVisitorStream;

import com.google.common.io.ByteSource;

/**
 * Byte source over a logical plan.
 */
public class ByteSourceOverCodecOp
    extends ByteSource
{
    protected CodecOp op;
    protected CodecOpVisitor<InputStream> streamVisitor;

    public ByteSourceOverCodecOp(CodecOp op) {
        this(op, CodecOpVisitorStream.getSingleton());
    }

    public ByteSourceOverCodecOp(CodecOp op, CodecOpVisitor<InputStream> streamVisitor) {
        this.op = Objects.requireNonNull(op);
        this.streamVisitor = Objects.requireNonNull(streamVisitor);
    }

    @Override
    public InputStream openStream() throws IOException {
        return op.accept(streamVisitor);
    }
}
