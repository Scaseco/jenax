package org.aksw.shellgebra.algebra.stream.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOp;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.SysRuntimeImpl;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class StreamOpVisitorStream
    implements StreamOpVisitor<InputStream>
{
    protected CompressorStreamFactory compressorStreamFactory;
    protected SysRuntime runtime;

    private static StreamOpVisitorStream singleton = null;

    public static StreamOpVisitorStream getSingleton() {
        if (singleton == null) {
            synchronized (StreamOpVisitorStream.class) {
                if (singleton == null) {
                    singleton = new StreamOpVisitorStream();
                }
            }
        }
        return singleton;
    }

    public StreamOpVisitorStream() {
        this(CompressorStreamFactory.getSingleton());
    }

    public StreamOpVisitorStream(CompressorStreamFactory compressorStreamFactory) {
        super();
        this.compressorStreamFactory = compressorStreamFactory;
    }

    @Override
    public InputStream visit(StreamOpFile op) {
        String name = op.getPath();
        Path path = Path.of(name);
        try {
            return Files.newInputStream(path, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream visit(StreamOpTranscode op) {
        InputStream base = op.getSubOp().accept(this);
        String name = op.getName();
        InputStream result;
        try {
            result = compressorStreamFactory.createCompressorInputStream(name, base, true);
        } catch (CompressorException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public InputStream visit(StreamOpConcat op) {
        List<StreamOp> subOps = op.getSubOps();
        Vector<InputStream> v = new Vector<>(subOps.size());
        for (StreamOp subOp : subOps) {
            InputStream contrib = subOp.accept(this);
            v.add(contrib);
        }
        return new SequenceInputStream(v.elements());
    }

    @Override
    public InputStream visit(StreamOpCommand op) {
        if (runtime == null) {
            throw new RuntimeException("No runtime set. Cannot execute command nodes.");
        }
        CmdOp cmdOp = op.getCmdOp();

        // The returned StringCmd can be either something that needs to be prefixed by a shell command
        // or it can be run directly.
        String[] cmd = runtime.compileCommand(cmdOp);

        List<String> c = new ArrayList<>();
        c.add("/usr/bin/bash");
        c.add("-c");

        // FIXME This is dirty
        c.add(SysRuntimeImpl.join(cmd));
        String[] d = c.toArray(new String[0]);

        try {
            return SystemUtils.exec(d);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // XXX Support setting a Function<StreamOpVar, StreamOp> resolver function.
    @Override
    public InputStream visit(StreamOpVar op) {
        throw new UnsupportedOperationException("Variable not supported: " + op);
    }

//    @Override
//    public InputStream visit(CodecOpCommandGroup op) {
//        List<CodecOp> subOps = op.getSubOps();
//        Vector<InputStream> v = new Vector<>(subOps.size());
//        for (CodecOp subOp : subOps) {
//            InputStream contrib = subOp.accept(this);
//            v.add(contrib);
//        }
//        return new SequenceInputStream(v.elements());
//    }
}
