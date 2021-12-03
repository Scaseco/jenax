package org.aksw.jenax.arq.connection.link;

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
