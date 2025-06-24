package org.aksw.shellgebra.algebra.common;

import java.util.Objects;

public record Transcoding(String name, TranscodeMode mode) {
    public Transcoding(String name, TranscodeMode mode) {
        this.name = Objects.requireNonNull(name);
        this.mode = Objects.requireNonNull(mode);
    }
}
