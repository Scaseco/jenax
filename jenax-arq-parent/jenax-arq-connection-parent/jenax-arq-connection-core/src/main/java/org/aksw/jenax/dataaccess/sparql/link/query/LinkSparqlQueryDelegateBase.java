package org.aksw.jenax.dataaccess.sparql.link.query;

import org.apache.jena.rdflink.LinkSparqlQuery;

public class LinkSparqlQueryDelegateBase
    implements LinkSparqlQueryDelegate
{
    protected LinkSparqlQuery delegate;

    public LinkSparqlQueryDelegateBase(LinkSparqlQuery delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public LinkSparqlQuery getDelegate() {
        return delegate;
    }

}
