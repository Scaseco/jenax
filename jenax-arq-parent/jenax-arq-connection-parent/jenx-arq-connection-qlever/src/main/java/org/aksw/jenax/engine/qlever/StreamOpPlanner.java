package org.aksw.jenax.engine.qlever;

import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jsheller.algebra.common.TranscodeMode;
import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.transform.StreamOpTransformExecutionPartitioner;
import org.aksw.jsheller.algebra.stream.transform.StreamOpVisitorFileName;
import org.aksw.jsheller.algebra.stream.transform.StreamOpVisitorFileName.FileName;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransformer;
import org.aksw.jsheller.registry.CodecRegistry;

public class StreamOpPlanner {
    public static String streamOpToFileName(StreamOp op) {
        StreamOpVisitorFileName fileNamer = new StreamOpVisitorFileName();
        FileName parts = op.accept(fileNamer);

        String suffix = parts.transcodings().stream().map(x -> {
            return (x.mode() == TranscodeMode.DECODE ? "un_" : "") + x.name();
        }).collect(Collectors.joining("."));

        return parts.baseName() + (suffix.isEmpty() ? "" : ".") + suffix;
    }

    public static void main(String[] args) {
        CodecRegistry codecRegistry = CodecRegistry.get();

        StreamOpTransformExecutionPartitioner xform = new StreamOpTransformExecutionPartitioner(codecRegistry);

        StreamOp op = new StreamOpTranscode("rot13", TranscodeMode.DECODE,
            new StreamOpTranscode("bzip2", TranscodeMode.DECODE,
                new StreamOpFile("/tmp/foo.bar")));

        StreamOpVisitorFileName fileNamer = new StreamOpVisitorFileName();

        StreamOp remainderOp = StreamOpTransformer.transform(op, xform);
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
