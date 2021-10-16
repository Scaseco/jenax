package org.aksw.jenax.stmt.parser.prologue;

import java.util.function.Function;

import org.apache.jena.sparql.core.Prologue;

public interface SparqlPrologueParser
    extends Function<String, Prologue>
{

}
