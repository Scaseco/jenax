package org.aksw.jenax.constraint.impl;

import org.aksw.jenax.constraint.api.ValueSpace;
import org.aksw.jenax.constraint.util.NodeRanges;

/**
 * An implementation of value space backed by {@link NodeRanges}.
 *
 * The parameters of the methods {@link #stateIntersection(ValueSpace)} and {@link #stateUnion(ValueSpace)}
 * only accept instances of {@link ValueSpaceImpl}. Any other type yields a {@link ClassCastException}.
 *
 * @author raven
 *
 */
public class ValueSpaceImpl
    implements ValueSpace
{
    protected NodeRanges nodeRanges;

    protected ValueSpaceImpl(NodeRanges nodeRanges) {
        super();
        this.nodeRanges = nodeRanges;
    }

    public static ValueSpaceImpl create(NodeRanges nodeRanges) {
        return new ValueSpaceImpl(nodeRanges);
    }

    @Override
    public ValueSpaceImpl clone() {
        return new ValueSpaceImpl(nodeRanges.clone());
    }

    @Override
    public ValueSpaceImpl stateIntersection(ValueSpace valueSpace) {
        ValueSpaceImpl that = (ValueSpaceImpl)valueSpace;
        nodeRanges.stateIntersection(that.nodeRanges);
        return this;
    }

    @Override
    public ValueSpaceImpl stateUnion(ValueSpace valueSpace) {
        ValueSpaceImpl that = (ValueSpaceImpl)valueSpace;
        nodeRanges.stateUnion(that.nodeRanges);
        return this;
    }

    @Override
    public boolean isConflicting() {
        return nodeRanges.isConflicting();
    }

}
