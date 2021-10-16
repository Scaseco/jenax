package org.aksw.jena_sparql_api.concept.parser;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;

import com.google.common.base.Function;

public interface SparqlRelationParser
    extends Function<String, BinaryRelation>
{

}
