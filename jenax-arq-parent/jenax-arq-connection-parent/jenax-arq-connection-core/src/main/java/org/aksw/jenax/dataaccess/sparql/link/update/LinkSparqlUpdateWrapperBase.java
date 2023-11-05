package org.aksw.jenax.dataaccess.sparql.link.update;

import org.apache.jena.rdflink.LinkSparqlUpdate;

public abstract class LinkSparqlUpdateWrapperBase
    implements LinkSparqlUpdateWrapper
{
    protected LinkSparqlUpdate delegate;

    public LinkSparqlUpdateWrapperBase(LinkSparqlUpdate delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public LinkSparqlUpdate getDelegate() {
        return delegate;
    }
}