package org.aksw.shellgebra.algebra.stream.transform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformExecutionPartitioner.Location;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpEntry;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransform;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformGeneric;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;

/**
 * Partitions stream expressions based on the capabilities of a runtime.
 * For example, let's assume a runtime can do rot13 encoding but not bzip2.
 * An execution partition comprises two results:
 * <ul>
 *   <li>A mapping of variables to operations that can be executed on the runtime.</li>
 *   <li>A remainder expression that makes use of these variables.</li>
 * </ul>
 *
 * Example:
 * <pre>
 * Original expression:
 * opTranscode(bzip2, encode, opTranscode(rot13, encode, opFile(foo.txt)))
 *
 * Remainder expression:
 * opTranscode(bzip2, encode, opVar(var1))
 *
 * Variable mapping:
 * var1 -&gt; opTranscode(rot13, encode, opFile(foo.txt)).
 * </pre>
 *
 */
public class StreamOpTransformExecutionPartitioner
    implements StreamOpTransformGeneric<StreamOpEntry<Location>>
{
    protected StreamOpTransform sysCallTransform;

    protected Map<String, StreamOp> varToOp = new LinkedHashMap<>();
    protected int nextVar = 0;

    public enum Location {
        NOT_HANDLED,
        HANDLED
    }

    public StreamOpTransformExecutionPartitioner(StreamOpTransform sysCallTransform) {
        super();
        this.sysCallTransform = Objects.requireNonNull(sysCallTransform);
    }

    public Map<String, StreamOp> getVarToOp() {
        return varToOp;
    }

    protected boolean isSupported(StreamOpTranscode op) {
        boolean result;
        try {
            StreamOp testOp = StreamOpTransformer.transform(op, sysCallTransform);
            result = testOp instanceof StreamOpCommand;
        } catch (Exception e) {
            // XXX Should check what exception we are getting
            result = false;
        }
        return result;
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpTranscode op, StreamOpEntry<Location> subOp) {
        StreamOpEntry<Location> result = null;
        // Location newOpLocation;
        // StreamOp newOp = subOp.getStreamOp();
        if (subOp.getValue() == Location.HANDLED) {
            boolean isSupported = isSupported(op);
            if (!isSupported) {
                StreamOp thisOp = new StreamOpTranscode(op.getTranscoding(), subOp.getStreamOp());
                result = injectVar(thisOp);
            }
        }

        if (result == null) {
            // StreamOp newOp = super.transform(op, subOp.getStreamOp());
            StreamOpTranscode newOp = new StreamOpTranscode(op.getTranscoding(), subOp.getStreamOp());
            result = new StreamOpEntry<>(newOp, subOp.getValue());
        }
        return result;
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpContentConvert op, StreamOpEntry<Location> subOp) {
        StreamOpEntry<Location> result = null;

        if (subOp.getValue() == Location.HANDLED) {
            boolean isSupported = false;
            if (!isSupported) {
                StreamOp thisOp = new StreamOpContentConvert(op.getSourceFormat(), op.getTargetFormat(), op.getBaseIri(), subOp.getStreamOp());
                result = injectVar(thisOp);
            }
        }

        if (result == null) {
            // StreamOp newOp = super.transform(op, subOp.getStreamOp());
            // StreamOpTranscode newOp = new StreamOpTranscode(op.getTranscoding(), subOp.getStreamOp());
            StreamOpContentConvert newOp = new StreamOpContentConvert(op.getSourceFormat(), op.getTargetFormat(), op.getBaseIri(), subOp.getStreamOp());
            result = new StreamOpEntry<>(newOp, subOp.getValue());
        }
        return result;    }

    protected StreamOpEntry<Location> injectVar(StreamOp thisOp) {
        StreamOpEntry<Location> result;
        String varName = "v" + (nextVar++);
        varToOp.put(varName, thisOp);
        StreamOpVar v = new StreamOpVar(varName);
        result = new StreamOpEntry<>(v, Location.NOT_HANDLED);
        return result;
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpFile op) {
        return new StreamOpEntry<>(op, Location.HANDLED);
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpVar op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpCommand op) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamOpEntry<Location> transform(StreamOpConcat op, List<StreamOpEntry<Location>> subOps) {
        throw new UnsupportedOperationException();
    }
}
