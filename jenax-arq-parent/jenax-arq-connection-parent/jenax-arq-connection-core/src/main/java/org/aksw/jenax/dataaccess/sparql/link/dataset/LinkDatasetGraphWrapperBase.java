package org.aksw.jenax.dataaccess.sparql.link.dataset;

import org.apache.jena.rdflink.LinkDatasetGraph;

public class LinkDatasetGraphWrapperBase<T extends LinkDatasetGraph>
    implements LinkDatasetGraphWrapper
{
    protected T delegate;

    public LinkDatasetGraphWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}
