package org.aksw.jenax.arq.connection.link;

import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;

public abstract class LinkSparqlQueryDelegate
    implements LinkSparqlQueryTmp
{
    public abstract LinkSparqlQuery getDelegate();

    @Override
    public QueryExecBuilder newQuery() {
        return getDelegate().newQuery();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public QueryExec query(Query query) {
        return getDelegate().query(query);
    }
}
