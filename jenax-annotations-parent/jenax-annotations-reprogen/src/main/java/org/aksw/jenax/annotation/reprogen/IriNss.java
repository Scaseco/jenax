package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IriNss {
    IriNs[] value();
}
