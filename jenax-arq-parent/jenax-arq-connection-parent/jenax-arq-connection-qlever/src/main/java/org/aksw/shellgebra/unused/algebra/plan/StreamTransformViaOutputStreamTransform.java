package org.aksw.shellgebra.unused.algebra.plan;

public class StreamTransformViaOutputStreamTransform
    implements StreamTransform
{
    protected OutputStreamTransform transform;

    public StreamTransformViaOutputStreamTransform(OutputStreamTransform transform) {
        super();
        this.transform = transform;
    }

    public OutputStreamTransform getTransform() {
        return transform;
    }
}
