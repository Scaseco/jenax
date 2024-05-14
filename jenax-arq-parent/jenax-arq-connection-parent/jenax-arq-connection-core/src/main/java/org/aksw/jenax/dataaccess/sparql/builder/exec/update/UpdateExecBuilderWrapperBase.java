package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import org.apache.jena.sparql.exec.UpdateExecBuilder;

public class UpdateExecBuilderWrapperBase
    implements UpdateExecBuilderWrapper
{
    protected UpdateExecBuilder delegate;

    public UpdateExecBuilderWrapperBase(UpdateExecBuilder delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateExecBuilder getDelegate() {
        return delegate;
    }
}
