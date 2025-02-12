package org.aksw.jsheller.algebra.stream.transform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.jsheller.algebra.stream.op.CodecSpec;
import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransformBase;
import org.aksw.jsheller.registry.CodecRegistry;

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
    extends StreamOpTransformBase
{
    protected CodecRegistry codecRegistry;

    protected Map<String, StreamOp> varToOp = new LinkedHashMap<>();
    protected int nextVar = 0;

    public StreamOpTransformExecutionPartitioner(CodecRegistry codecRegistry) {
        super();
        this.codecRegistry = Objects.requireNonNull(codecRegistry);
    }

    public Map<String, StreamOp> getVarToOp() {
        return varToOp;
    }

    protected boolean isSupported(StreamOpTranscode op) {
        CodecSpec codecSpec = codecRegistry.getCodecSpec(op.getName());
        boolean result = codecSpec != null;
        return result;
    }

    @Override
    public StreamOp transform(StreamOpTranscode op, StreamOp subOp) {
        StreamOp result;
        boolean isSupported = isSupported(op);
        if (!isSupported) {
            String varName = "v" + (nextVar++);
            varToOp.put(varName, subOp);
            StreamOpVar v = new StreamOpVar(varName);
            result = new StreamOpTranscode(op.getName(), op.getTranscodeMode(), v);
        } else {
            result = super.transform(op, subOp);
        }
        return result;
    }
}
