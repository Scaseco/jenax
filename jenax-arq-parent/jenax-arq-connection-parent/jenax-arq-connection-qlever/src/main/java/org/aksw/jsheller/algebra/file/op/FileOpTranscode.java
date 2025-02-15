package org.aksw.jsheller.algebra.file.op;

import java.util.Objects;

import org.aksw.jsheller.algebra.common.TranscodeMode;

public class FileOpTranscode
    extends FileOp1
{
    protected String codecName;
    protected TranscodeMode transcodeMode;

    public FileOpTranscode(String codecName, TranscodeMode transcodeMode, FileOp subOp) {
        super(subOp);
        this.codecName = Objects.requireNonNull(codecName);
        this.transcodeMode = Objects.requireNonNull(transcodeMode);
    }

    public String getCodecName() {
        return codecName;
    }

    public TranscodeMode getTranscodeMode() {
        return transcodeMode;
    }

    @Override
    public <T> T accept(FileOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
