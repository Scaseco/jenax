package org.aksw.jenax.annotation.reprogen;

public @interface IdPrefix {
    String value() default "";
    String separator() default "-";
}
