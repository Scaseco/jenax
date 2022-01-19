package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Namespaces {
    Namespace[] value(); // TODO is the default null or empty array? should be empty array.
}
