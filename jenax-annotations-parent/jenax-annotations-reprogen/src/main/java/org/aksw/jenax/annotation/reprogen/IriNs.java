package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specify a namespace to which the property name is appended in order to yield the final URL
 *
 * <pre>
 * interface Person extends Resource {
 *   @IriNs("http://www.example.org/")
 *   String getName(); // RDF predicate will be <http://www.example.org/name>
 * }
 * </pre>
 *
 * @author Claus Stadler, Oct 9, 2018
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IriNs {
    String[] value() default { "" };
}
