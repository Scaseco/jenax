package org.aksw.jenax.sparql.algebra.walker;

import org.aksw.commons.path.core.Path;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.TransformCopy;

public class TrackingTransformCopy<T>
    extends TransformCopy
{
    protected Tracker<T> tracker;

    protected Path<String> path() {
        return tracker.getPath();
    }

    public TrackingTransformCopy(Tracker<T> pathState) {
        this(pathState, false);
    }

    public TrackingTransformCopy(Tracker<T> pathState, boolean alwaysDuplicate) {
        super(alwaysDuplicate);
        this.tracker = pathState;
    }

    public OpVisitor getBeforeVisitor() {
        return null;
    }

}
