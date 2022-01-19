package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    // Class<? extends RDFNode would require dependency on jena
    Class<?>[] value();
    boolean useCanAs = true;
}
