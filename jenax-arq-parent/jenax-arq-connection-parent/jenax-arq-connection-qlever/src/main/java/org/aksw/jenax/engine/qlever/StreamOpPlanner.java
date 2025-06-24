package org.aksw.jenax.engine.qlever;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.stream.op.CodecSysEnv;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformExecutionPartitioner;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformToCmdOp;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpVisitorFileName;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpTransformExecutionPartitioner.Location;
import org.aksw.shellgebra.algebra.stream.transform.StreamOpVisitorFileName.FileName;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpEntry;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.aksw.shellgebra.registry.CodecRegistry;

public class StreamOpPlanner {
    public static String streamOpToFileName(StreamOp op) {
        String result = streamOpToFileName(op, null);
        return result;
    }

    public static String streamOpToFileName(StreamOp op, Function<String, ? extends StreamOp> varResolver) {
        StreamOpVisitorFileName fileNamer = new StreamOpVisitorFileName(varResolver);
        FileName parts = op.accept(fileNamer);

        String suffix = parts.transcodings().stream().map(x -> {
            return (x.mode() == TranscodeMode.DECODE ? "un_" : "") + x.name();
        }).collect(Collectors.joining("."));

        return parts.baseName() + (suffix.isEmpty() ? "" : ".") + suffix;
    }

    public static void main(String[] args) {
        // The codec registry describes codecs - but not whether they are present in
        // a runtime environment
        CodecRegistry codecRegistry = CodecRegistry.get();
        CodecSysEnv env = new CodecSysEnv(SysRuntimeImpl.forCurrentOs());

        StreamOpTransformToCmdOp sysCallTransform = new StreamOpTransformToCmdOp(codecRegistry, env);
        StreamOpTransformExecutionPartitioner xform = new StreamOpTransformExecutionPartitioner(sysCallTransform);

        StreamOp op = new StreamOpTranscode("rot13", TranscodeMode.DECODE,
            new StreamOpTranscode("bzip2", TranscodeMode.DECODE,
                new StreamOpFile("/tmp/foo.bar")));

        StreamOpVisitorFileName fileNamer = new StreamOpVisitorFileName();

        // remainderOp: TRUE means that the expression
        StreamOpEntry<Location> remainderOp = StreamOpTransformer.transform(op, xform);
        Map<String, StreamOp> varMap = xform.getVarToOp();
        System.out.println(remainderOp);
        System.out.println(varMap);

        StreamOp fop = varMap.values().iterator().next();
        String outFile = streamOpToFileName(fop);
        System.out.println(outFile);



        // FileName name = varMap.values().iterator().next().accept(fileNamer);
        // System.out.println(name);
    }
}
