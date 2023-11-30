package org.aksw.jenax.dataaccess.sparql.link.query;

import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/**
 * Wrapper that delegates all query methods to the delegate's {@link LinkSparqlQueryBase#newQuery()} method.
 */
public interface LinkSparqlQueryWrapper
    extends LinkSparqlQueryBase
{
    @Override
    LinkSparqlQuery getDelegate();

    @Override
    default QueryExecBuilder newQuery() {
        return getDelegate().newQuery();
    }

    @Override
    default void close() {
        getDelegate().close();
    }
}
