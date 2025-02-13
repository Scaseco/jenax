package org.aksw.jsheller.algebra.stream.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.aksw.jsheller.algebra.common.Transcoding;
import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpCommand;
import org.aksw.jsheller.algebra.stream.op.StreamOpConcat;
import org.aksw.jsheller.algebra.stream.op.StreamOpFile;
import org.aksw.jsheller.algebra.stream.op.StreamOpTranscode;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;
import org.aksw.jsheller.algebra.stream.op.StreamOpVisitor;
import org.aksw.jsheller.algebra.stream.transform.StreamOpVisitorFileName.FileName;

/** Collects file name and transcodings into a flat list. */
public class StreamOpVisitorFileName
    implements StreamOpVisitor<FileName>
{
    public static record FileName(
        String baseName,
        List<Transcoding> transcodings
    ) {}

    protected Function<String, ? extends StreamOp> varNameResolver;

    public StreamOpVisitorFileName() {
        this(null);
    }

    public StreamOpVisitorFileName(Function<String, ? extends StreamOp> varNameResolver) {
        super();
        this.varNameResolver = varNameResolver;
    }

    @Override
    public FileName visit(StreamOpFile op) {
        return new FileName(op.getPath(), List.of());
    }

    @Override
    public FileName visit(StreamOpTranscode op) {
        FileName base = op.getSubOp().accept(this);
        List<Transcoding> transcodings = new ArrayList<>(base.transcodings());
        transcodings.add(op.getTranscoding());
        return new FileName(base.baseName(), List.copyOf(transcodings));
    }

    @Override
    public FileName visit(StreamOpConcat op) {
        throw new UnsupportedOperationException("Filename generation not implemented for this operator: " + op);
    }

    @Override
    public FileName visit(StreamOpCommand op) {
        throw new UnsupportedOperationException("Filename generation not implemented for this operator: " + op);
    }

    @Override
    public FileName visit(StreamOpVar op) {
        FileName result;
        if (varNameResolver != null) {
            String varName = op.getVarName();
            StreamOp subOp = varNameResolver.apply(varName);
            if (subOp != null) {
                result = subOp.accept(this);
            } else {
                throw new RuntimeException("Variable name " + varName + " could not be resolved to a sub op.");
            }
        } else {
            throw new UnsupportedOperationException("Filename generation not implemented for this operator: " + op);
        }
        return result;
    }
}
