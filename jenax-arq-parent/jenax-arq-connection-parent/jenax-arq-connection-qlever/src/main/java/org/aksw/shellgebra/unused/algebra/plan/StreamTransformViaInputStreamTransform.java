package org.aksw.shellgebra.unused.algebra.plan;

public class StreamTransformViaInputStreamTransform
    implements StreamTransform
{
    protected InputStreamTransform transform;

    public StreamTransformViaInputStreamTransform(InputStreamTransform transform) {
        super();
        this.transform = transform;
    }

    public InputStreamTransform getTransform() {
        return transform;
    }
}
