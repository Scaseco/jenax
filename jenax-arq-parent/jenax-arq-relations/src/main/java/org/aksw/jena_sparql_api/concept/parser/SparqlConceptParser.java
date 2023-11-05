package org.aksw.jena_sparql_api.concept.parser;

import java.util.function.Function;

import org.aksw.jenax.sparql.fragment.impl.Concept;

public interface SparqlConceptParser
    extends Function<String, Concept>
{

}
