package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Generate an RDF type triple using the annotated class's fully qualified name.
 * For example, from a class package.name.ClassName the IRI java://package.name.ClassName is derived.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfType {
    String value() default "";
}
