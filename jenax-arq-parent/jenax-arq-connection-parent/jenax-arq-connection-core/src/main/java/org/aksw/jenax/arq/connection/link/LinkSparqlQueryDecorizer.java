package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.rdflink.LinkSparqlQuery;

/**
 * A "decorizer" is a factory for decorators.
 * Transforms a {@link LinkSparqlQuery} into another one typically by decorating it. */
@FunctionalInterface
public interface LinkSparqlQueryDecorizer
    extends Function<LinkSparqlQuery, LinkSparqlQuery>
{
}
