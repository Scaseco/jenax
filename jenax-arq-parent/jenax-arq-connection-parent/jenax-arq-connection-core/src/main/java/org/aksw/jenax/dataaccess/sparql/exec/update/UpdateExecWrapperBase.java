package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.update.UpdateProcessor;

public class UpdateExecWrapperBase<T extends UpdateProcessor>
    implements UpdateExecWrapper
{
    protected T delegate;

    public UpdateExecWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateProcessor getDelegate() {
        return delegate;
    }

//    protected void beforeExec() {
//
//    }
//
//    protected void afterExec() {
//
//    }
//
//    protected void onException(Exception e) {
//    }

//    @Override
//    public void execute() {
//        beforeExec();
//        try {
//            delegate.execute();
//        } catch(Exception e) {
//            onException(e);
//            throw e;
//        } finally {
//            afterExec();
//        }
//    }
}
