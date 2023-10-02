package org.aksw.jenax.arq.util.fmt;

import org.apache.jena.query.QueryType;

/**
 * Result set format type.
 * Note, that this enum is similar to {@link QueryType} but the difference is that
 * the query types "DESCRIBE" and "CONSTRUCT" are conflated into "Triples".
 */
public enum SparqlResultType {
    Unknown, AskResult /* Boolean */, Bindings, Triples, Quads
}
