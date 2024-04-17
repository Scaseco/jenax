package org.aksw.jenax.dataaccess.sparql.execution.update;

import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorWrapperBase<T extends UpdateProcessor>
    implements UpdateProcessorWrapper
{
    protected T delegate;

    public UpdateProcessorWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateProcessor getDelegate() {
        return delegate;
    }

    protected void beforeExec() {

    }

    protected void afterExec() {

    }

    protected void onException(Exception e) {
        // throw new RuntimeException(e);
    }

    @Override
    public void execute() {
        beforeExec();
        try {
            delegate.execute();
        } catch(Exception e) {
            onException(e);
            throw e;
        } finally {
            // FIXME If there was an exception and afterExec throws one as well then the first one gets swallowed!
            afterExec();
        }
    }
}
