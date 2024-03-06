package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default value annotation for use in generation of SPARQL functions from
 * Java methods.
 * The provided string must be either a valid lexical value for the arguments datatype
 * corresponding RDF type.
 *
 * For example, Java's primitive {@code boolean} and boxed {@code Boolean} types both map to {@code xsd:boolean}.
 * Its lexical space is the set of strings {"false", "true" }.
 * Hence, the annotation {@code myMethod(@DefaultValue("true") boolean arg)} is interpreted as {@code "true"^^xsd:boolean}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DefaultValue {
    String value();
}
