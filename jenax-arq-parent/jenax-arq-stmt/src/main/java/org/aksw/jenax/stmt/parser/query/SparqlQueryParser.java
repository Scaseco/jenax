package org.aksw.jenax.stmt.parser.query;

import java.util.function.Function;

import org.apache.jena.query.Query;

public interface SparqlQueryParser
    extends Function<String, Query>
{
    // TODO May add method to get prefix configuration
}
