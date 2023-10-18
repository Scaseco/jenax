package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateProcessor;

public class UpdateExecDecoratorBase<T extends UpdateExec>
    implements UpdateExecDecorator
{
    protected T delegate;

    public UpdateExecDecoratorBase(T delegate) {
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
            afterExec();
        }
    }
}
