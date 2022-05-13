package org.aksw.jenax.arq.connection.link;

import java.util.function.Function;

import org.apache.jena.rdflink.LinkSparqlQuery;

/** Transform a {@link LinkSparqlQuery} into another one*/
@FunctionalInterface
public interface LinkSparqlQueryMod
	extends Function<LinkSparqlQuery, LinkSparqlQuery>
{
}
