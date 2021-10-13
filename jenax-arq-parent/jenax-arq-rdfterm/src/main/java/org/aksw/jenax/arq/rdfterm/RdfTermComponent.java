package org.aksw.jenax.arq.rdfterm;

/**
 * Constants for referring to components/aspects of rdf terms.
 *
 * An RDF term is characterized by the term type, the lexical form,
 * and for literals the datatype and langague tag.
 *
 * In addition VALUE is provided to refer to the value denoted an rdf term type.
 *
 * For example, a literal with lexical form of "1" and datatype  xsd:int
 * refers to the numeric value 1.
 *
 * @author raven
 *
 */
public enum RdfTermComponent {
    UNKNOWN,
    TERM_TYPE,
    LEXICAL_FORM,
    DATATYPE,
    LANGUAGE_TAG,
    VALUE
}
