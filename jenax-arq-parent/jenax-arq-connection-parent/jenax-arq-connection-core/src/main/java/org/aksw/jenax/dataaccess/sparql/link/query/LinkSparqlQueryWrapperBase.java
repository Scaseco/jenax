package org.aksw.jenax.dataaccess.sparql.link.query;

import org.apache.jena.rdflink.LinkSparqlQuery;

public class LinkSparqlQueryWrapperBase
    implements LinkSparqlQueryWrapper
{
    protected LinkSparqlQuery delegate;

    public LinkSparqlQueryWrapperBase(LinkSparqlQuery delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public LinkSparqlQuery getDelegate() {
        return delegate;
    }
}
