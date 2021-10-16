package org.aksw.jenax.stmt.parser.update;

import java.util.function.Function;

import org.apache.jena.update.UpdateRequest;

public interface SparqlUpdateParser
    extends Function<String, UpdateRequest>
{
}
