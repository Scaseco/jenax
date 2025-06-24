package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpVisitorStream;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpVisitorWrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

/**
 * Byte source over a logical plan.
 */
public class ByteSourceOverStreamOp
    extends ByteSource
{
    private static final Logger logger = LoggerFactory.getLogger(ByteSourceOverStreamOp.class);

    protected StreamOp op;

    // TODO hostOps may be executed on a visitor that supplies a different environment.
    protected Map<String, StreamOp> hostOps;

    protected StreamOpVisitor<InputStream> streamVisitor;

    public ByteSourceOverStreamOp(StreamOp op) {
        this(op, null);
    }

    public ByteSourceOverStreamOp(StreamOp op, Map<String, StreamOp> hostOps) {
        this(op, hostOps, StreamOpVisitorStream.getSingleton());
    }

    public ByteSourceOverStreamOp(StreamOp op, Map<String, StreamOp> hostOps, StreamOpVisitor<InputStream> streamVisitor) {
        this.op = Objects.requireNonNull(op);
        this.hostOps = hostOps;
        this.streamVisitor = Objects.requireNonNull(streamVisitor);
    }

    @Override
    public InputStream openStream() throws IOException {
        logger.info("Serving input stream from algebra eval: " + op + " with vars: " + hostOps);

        StreamOpVisitor<InputStream> visitor = streamVisitor;
        if (hostOps != null && !hostOps.isEmpty()) {
            visitor = new StreamOpVisitorWrapperBase<>(streamVisitor) {
                @Override
                public InputStream visit(StreamOpVar op) {
                    String varName = op.getVarName();
                    StreamOp nextOp = hostOps.get(varName);
                    if (nextOp == null) {
                        throw new RuntimeException("Variable not defined: " + varName);
                    }
                    logger.info("Sub-executing: " + varName + " <- " + nextOp);
                    InputStream result = nextOp.accept(this);
                    return result;
                }
            };
        }

        return op.accept(visitor);
    }
}
