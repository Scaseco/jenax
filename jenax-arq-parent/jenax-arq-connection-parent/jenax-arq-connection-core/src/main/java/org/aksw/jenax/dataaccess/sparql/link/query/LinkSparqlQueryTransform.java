package org.aksw.jenax.dataaccess.sparql.link.query;

import java.util.function.Function;

import org.apache.jena.rdflink.LinkSparqlQuery;

/** Transforms a {@link LinkSparqlQuery} into another one typically by decorating it. */
@FunctionalInterface
public interface LinkSparqlQueryTransform
    extends Function<LinkSparqlQuery, LinkSparqlQuery>
{
}
