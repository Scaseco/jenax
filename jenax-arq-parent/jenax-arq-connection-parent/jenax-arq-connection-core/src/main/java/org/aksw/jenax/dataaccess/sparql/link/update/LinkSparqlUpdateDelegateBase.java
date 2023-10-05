package org.aksw.jenax.dataaccess.sparql.link.update;

import org.apache.jena.rdflink.LinkSparqlUpdate;

public abstract class LinkSparqlUpdateDelegateBase
    implements LinkSparqlUpdateDelegate
{
    protected LinkSparqlUpdate delegate;

    public LinkSparqlUpdateDelegateBase(LinkSparqlUpdate delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public LinkSparqlUpdate getDelegate() {
        return delegate;
    }
}