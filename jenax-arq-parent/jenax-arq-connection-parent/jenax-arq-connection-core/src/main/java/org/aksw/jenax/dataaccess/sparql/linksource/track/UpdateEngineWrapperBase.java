package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.sparql.modify.UpdateEngine;

public class UpdateEngineWrapperBase
    implements UpdateEngineWrapper
{
    protected UpdateEngine delegate;

    public UpdateEngineWrapperBase(UpdateEngine delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateEngine getDelegate() {
        return delegate;
    }
}
