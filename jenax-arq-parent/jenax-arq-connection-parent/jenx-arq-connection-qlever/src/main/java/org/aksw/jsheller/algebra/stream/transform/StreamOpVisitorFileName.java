package org.aksw.jsheller.algebra.stream.transform;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jsheller.algebra.common.Transcoding;
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
        throw new UnsupportedOperationException("Filename generation not implemented for this operator: " + op);
    }
}
