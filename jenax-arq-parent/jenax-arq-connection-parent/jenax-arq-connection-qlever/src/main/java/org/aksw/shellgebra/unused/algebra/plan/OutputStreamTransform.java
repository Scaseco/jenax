package org.aksw.shellgebra.unused.algebra.plan;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface OutputStreamTransform {

    OutputStream apply(OutputStream out) throws IOException;

    default InputStreamTransform asInputStreamTransform() {
        return new InputStreamTransformOverOutputStreamTransform(this);
    }

    /** False if 'this' is the 'native' transform. True for the piped transform return by asInputStreamTransform. */
    default boolean isPiped() {
        return false;
    }
}
