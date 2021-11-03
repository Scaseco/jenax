package org.aksw.jena_sparql_api.views;

/**
 * RDF term types
 *
 * @author raven
 *
 */
public enum RdfTermType {
    UNKNOWN,
    BLANK,
    IRI, // TODO This should probably be resource (i.e. uri + blank node)
    LITERAL,
    //PLAIN_LITERAL,
    //TYPED_LITERAL,
}