package org.aksw.shellgebra.exec;

public class SysRuntimeWrapperBase<X extends SysRuntime>
    implements SysRuntimeWrapper<X>
{
    protected X delegate;

    public SysRuntimeWrapperBase(X delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public X getDelegate() {
        return delegate;
    }
}
