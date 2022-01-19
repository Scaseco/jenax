package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Namespace {
//    String ns();
//    String iri();
	String prefix();
    String value();
}
