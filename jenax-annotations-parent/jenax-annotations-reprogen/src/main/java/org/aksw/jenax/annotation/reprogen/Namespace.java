package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Namespaces.class)
public @interface Namespace {
    String prefix() default "";
    String value();
}
