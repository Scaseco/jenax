package org.aksw.jsheller.algebra.logical;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.aksw.jsheller.algebra.physical.CmdOp;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import jenax.engine.qlever.SystemUtils;

public class CodecOpVisitorStream
    implements CodecOpVisitor<InputStream>
{
    protected CompressorStreamFactory compressorStreamFactory;
    protected SysRuntime runtime;

    private static CodecOpVisitorStream singleton = null;

    public static CodecOpVisitorStream getSingleton() {
        if (singleton == null) {
            synchronized (CodecOpVisitorStream.class) {
                if (singleton == null) {
                    singleton = new CodecOpVisitorStream();
                }
            }
        }
        return singleton;
    }

    public CodecOpVisitorStream() {
        this(CompressorStreamFactory.getSingleton());
    }

    public CodecOpVisitorStream(CompressorStreamFactory compressorStreamFactory) {
        super();
        this.compressorStreamFactory = compressorStreamFactory;
    }

    @Override
    public InputStream visit(CodecOpFile op) {
        String name = op.getPath();
        Path path = Path.of(name);
        try {
            return Files.newInputStream(path, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream visit(CodecOpCodecName op) {
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
    public InputStream visit(CodecOpConcat op) {
        List<CodecOp> subOps = op.getSubOps();
        Vector<InputStream> v = new Vector<>(subOps.size());
        for (CodecOp subOp : subOps) {
            InputStream contrib = subOp.accept(this);
            v.add(contrib);
        }
        return new SequenceInputStream(v.elements());
    }

    @Override
    public InputStream visit(CodecOpCommand op) {
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
