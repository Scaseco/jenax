package org.aksw.jenax.arq.connection.link;

import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public interface LinkSparqlQueryDelegate
    extends LinkSparqlQueryTmp
{
    public LinkSparqlQuery getDelegate();

    @Override
    default QueryExecBuilder newQuery() {
        return getDelegate().newQuery();
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default QueryExec query(Query query) {
        return getDelegate().query(query);
    }
}
