package org.aksw.jenax.sparql.algebra.walker;

import org.aksw.commons.path.core.Path;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.TransformCopy;

public class TrackingTransformCopy
    extends TransformCopy
{
    protected Tracker pathState;

    protected Path<String> path() {
        return pathState.getPath();
    }

    public TrackingTransformCopy(Tracker pathState) {
        this(pathState, false);
    }

    public TrackingTransformCopy(Tracker pathState, boolean alwaysDuplicate) {
        super(alwaysDuplicate);
        this.pathState = pathState;
    }

    public OpVisitor getBeforeVisitor() {
        return null;
    }

}
