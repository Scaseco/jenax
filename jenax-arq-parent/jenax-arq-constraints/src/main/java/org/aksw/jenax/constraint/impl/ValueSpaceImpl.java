package org.aksw.jenax.constraint.impl;

import java.util.Set;

import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.ValueSpace;
import org.aksw.jenax.constraint.util.NodeRanges;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

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

    public NodeRanges getNodeRanges() {
        return nodeRanges;
    }


    @Override
    public ValueSpaceImpl clone() {
        return new ValueSpaceImpl(nodeRanges.clone());
    }

    @Override
    public boolean isLimitedTo(Object dimensionKey) {
        Set<?> vss = nodeRanges.getValueSpaces();

        boolean result = nodeRanges.isVscExhaustive() && vss.size() == 1 && vss.contains(dimensionKey);
        return result;
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

    @Override
    public String toString() {
        return nodeRanges.toString();
    }



    /** TODO The factory-aspect of creating a new ValueSpace with an open dimension should go to ValueSpaceSchema */
    @Override
    public ValueSpace forDimension(Object dimensionKey) {
        NodeRanges tmp = NodeRanges.createClosed();
        tmp.addOpenDimension(dimensionKey);
        ValueSpace vc = ValueSpaceImpl.create(tmp);
        ValueSpace result = vc.stateIntersection(this);
        return result;
    }

    @Override
    public ValueSpace moveDimension(Object fromDimKey, Object toDimKey) {
        RangeSet<ComparableNodeValue> ranges = nodeRanges.getDimension(fromDimKey);
        nodeRanges.removeDimension(fromDimKey); // .setDimension(fromDimKey, null);
        nodeRanges.setDimension(toDimKey, ranges);
        return this;
    }


}
