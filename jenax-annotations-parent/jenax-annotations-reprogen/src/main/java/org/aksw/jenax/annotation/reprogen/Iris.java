package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Container for multiple {@link Iri} annotations.
 * Typical use for multiple annotations is to support use of both new and legacy IRIs.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Iris {
    Iri[] value();
}
