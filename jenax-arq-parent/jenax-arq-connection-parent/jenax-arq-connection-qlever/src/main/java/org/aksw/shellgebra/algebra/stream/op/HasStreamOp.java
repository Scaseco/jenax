package org.aksw.shellgebra.algebra.stream.op;

import org.aksw.shellgebra.algebra.stream.transformer.StreamOpEntry;
import org.aksw.shellgebra.algebra.stream.transformer.StreamOpTransformGeneric;

/**
 * Interface to access an object's effective StreamOp.
 * Used in {@link StreamOpTransformGeneric} to allow for grouping ops with
 * custom data, such as using {@link StreamOpEntry}.
 */
public interface HasStreamOp {
    StreamOp getStreamOp();
}
