package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation has different semantics depending on where it appears.
 * The following rules are taken from Alibaba for compatibility reasons:
 * <ul>
 *   <li>Class level: the RDF type</li>
 *   <li>Attribute: the RDF property</li>
 *   <li>Method: Allows for reference to a method by IRI. This is mainly used for reflection-based derivation of SPARQL extension functions from annotated functions.</li>
 *   <li>Method Parameter: Allows reference for a function. This is mainly used for reflection-based "function ontology" derivations from method paramaters.</li>
 * </ul>
 *
 * If the value is an empty string, the annotated property's name
 * will be treated as a relative IRI.
 */
@Retention(RetentionPolicy.RUNTIME)
// @Repeatable(Iris.class) // TODO Switch to repeatable annotation!
public @interface Iri {
    String[] value() default { "" };
}
