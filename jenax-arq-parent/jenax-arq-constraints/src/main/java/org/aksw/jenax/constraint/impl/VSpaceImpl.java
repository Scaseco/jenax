package org.aksw.jenax.constraint.impl;

import java.util.Set;
import java.util.function.Function;

import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.util.NodeRanges;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * An implementation of value space backed by {@link NodeRanges}.
 *
 * The parameters of the methods {@link #stateIntersection(VSpace)} and {@link #stateUnion(VSpace)}
 * only accept instances of {@link VSpaceImpl}. Any other type yields a {@link ClassCastException}.
 *
 * @author raven
 *
 */
public class VSpaceImpl
    implements VSpace
{
    protected NodeRanges nodeRanges;

    protected VSpaceImpl(NodeRanges nodeRanges) {
        super();
        this.nodeRanges = nodeRanges;
    }

    public static VSpaceImpl create(NodeRanges nodeRanges) {
        return new VSpaceImpl(nodeRanges);
    }

    public NodeRanges getNodeRanges() {
        return nodeRanges;
    }


    @Override
    public VSpaceImpl clone() {
        return new VSpaceImpl(nodeRanges.clone());
    }

    @Override
    public boolean isLimitedTo(Object dimensionKey) {
        Set<?> vss = nodeRanges.getValueSpaces();

        boolean result = nodeRanges.isVscExhaustive() && vss.size() == 1 && vss.contains(dimensionKey);
        return result;
    }

    @Override
    public VSpaceImpl stateIntersection(VSpace valueSpace) {
        VSpaceImpl that = (VSpaceImpl)valueSpace;
        nodeRanges.stateIntersection(that.nodeRanges);
        return this;
    }

    @Override
    public VSpaceImpl stateUnion(VSpace valueSpace) {
        VSpaceImpl that = (VSpaceImpl)valueSpace;
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
    public VSpace forDimension(Object dimensionKey) {
        NodeRanges tmp = NodeRanges.createClosed();
        tmp.addOpenDimension(dimensionKey);
        VSpace vc = VSpaceImpl.create(tmp);
        VSpace result = vc.stateIntersection(this);
        return result;
    }

    @Override
    public <X extends Comparable<X>> VSpace mapDimensionToNewVSpace(Object fromDimKey, Class<X> itemType,
            Function<Range<X>, Range<X>> mapper) {
        Preconditions.checkArgument(ComparableNodeValue.class.isAssignableFrom(itemType), "Type must be ComparableNodeValue.class");
        RangeSet<ComparableNodeValue> ranges = nodeRanges.getDimension(fromDimKey);
        NodeRanges newNodeRanges = NodeRanges.createClosed();
        for (Range<ComparableNodeValue> range : ranges.asRanges()) {
            Range<ComparableNodeValue> out = (Range<ComparableNodeValue>) mapper.apply((Range<X>)range);
            newNodeRanges.add(out);
        }
        return VSpaceImpl.create(newNodeRanges);
    }

//    @Override
//    public VSpace moveDimension(Object fromDimKey, Object toDimKey) {
//        RangeSet<ComparableNodeValue> ranges = nodeRanges.getDimension(fromDimKey);
//        nodeRanges.removeDimension(fromDimKey); // .setDimension(fromDimKey, null);
//        nodeRanges.setDimension(toDimKey, ranges);
//        return this;
//    }
}
