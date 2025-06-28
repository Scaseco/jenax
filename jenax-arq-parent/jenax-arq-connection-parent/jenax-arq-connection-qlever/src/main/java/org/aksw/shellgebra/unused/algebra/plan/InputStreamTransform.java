package org.aksw.shellgebra.unused.algebra.plan;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamTransform {

    InputStream apply(InputStream in) throws IOException;

    default OutputStreamTransform asOutputStreamTransform() {
        return new OutputStreamTransformOverInputStreamTransform(this);
    }

    /** False if 'this' is the 'native' transform. True for the piped transform return by asOutputStreamTransform. */
    default boolean isPiped() {
        return false;
    }
}
