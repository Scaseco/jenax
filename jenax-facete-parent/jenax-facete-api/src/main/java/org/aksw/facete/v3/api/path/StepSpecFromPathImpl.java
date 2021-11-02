package org.aksw.facete.v3.api.path;

public class StepSpecFromPathImpl<T>
    implements StepSpecFromPath
{
    protected T path;

    public StepSpecFromPathImpl(T path) {
        super();
        this.path = path;
    }

    @Override
    public T getPath() {
        return path;
    }
}
