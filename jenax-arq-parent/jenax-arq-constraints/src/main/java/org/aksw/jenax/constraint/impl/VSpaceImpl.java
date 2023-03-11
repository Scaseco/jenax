package org.aksw.jenax.constraint.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.algebra.allen.AllenRelation;
import org.aksw.commons.algebra.allen.AllenRelations;
import org.aksw.commons.util.range.Cmp;
import org.aksw.commons.util.range.CmpFactory;
import org.aksw.commons.util.range.Endpoint;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.Domain;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.sparql.expr.ValueSpace;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

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
    protected Domain<ValueSpace, ComparableNodeValue> domain;
    protected NodeRanges nodeRanges;

    protected VSpaceImpl(NodeRanges nodeRanges) {
        super();
        this.domain = DomainNodeValue.get();
        this.nodeRanges = nodeRanges;
    }

    @Override
    public Domain<?, ?> getDomain() {
        return domain;
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
        // NodeRanges nr = ((VSpaceImpl)fromDimKey).getNodeRanges();

        RangeSet<ComparableNodeValue> ranges = nodeRanges.getDimension(fromDimKey);
        if (ranges == null) {
            ranges = TreeRangeSet.create();
        }

        NodeRanges newNodeRanges = NodeRanges.createClosed();
        for (Range<ComparableNodeValue> range : ranges.asRanges()) {
            Range<ComparableNodeValue> out = (Range<ComparableNodeValue>) mapper.apply((Range<X>)range);
            newNodeRanges.add(out);
        }
        return VSpaceImpl.create(newNodeRanges);
    }


    @Override
    public AllenRelation relateTo(VSpace that) {

        Range<Cmp<Entry<?, Cmp<ComparableNodeValue>>>> thisSpan = span(this);
        Range<Cmp<Entry<?, Cmp<ComparableNodeValue>>>> thatSpan = span(that);

        AllenRelation result = AllenRelations.compute(thisSpan, thatSpan);
        return result;
//        NodeRanges a = this.nodeRanges;
//        NodeRanges b = ((VSpaceImpl)that).nodeRanges;
//
//        // If both vscs are exhaustive: Iterate the union of the dimension keys and to the checks
//        // If neither vsc is exhaustive: Iterate the domain's dimension keys and do the checks
//            // typically this should quickly lead to an overlap -alternatively: just return ovelap directly?
//        // If one is exhaustive:
//
//        AllenRelation result = AllenRelation.empty();
//        List<?> vscs;
//        if (a.isVscExhaustive() && b.isVscExhaustive()) {
//            vscs = new ArrayList<>(Sets.union(a.getValueSpaces(), b.getValueSpaces()));
//        } else {
//            vscs = new ArrayList<>(domain.getDimensions());
//        }
//
//        Collections.sort(vscs, domain.getDimensionComparatorRaw());
//
////        CmpValueFactory.<Entry<Object, ComparableNodeValue>>of((x, y -> {
////        	return ComparisonChain.start()
////        			.compare(x.getKey(), y.getKey(), domain.getDimensionComparatorRaw())
////        			.compare()
////        });
//
//
//
//        for (Object vsc : vscs) {
//            RangeSet<ComparableNodeValue> ar = a.getDimension(vsc);
//            RangeSet<ComparableNodeValue> br = b.getDimension(vsc);
//
//            if (ar == null || ar.isEmpty()) {
//                if (br == null || br.isEmpty()) {
//                    continue;
//                } else {
//
//                    result = result.union(AllenRelation.AFTER);
//                    continue;
//                }
//            } else {
//                if (hasSeenOtherRange) {
//                    isBefore = false;
//                }
//
//                if (br == null || br.isEmpty()) {
//                    result = result.union(AllenRelation.BEFORE);
//                } else {
//                    Range<ComparableNodeValue> firstA = ar.asRanges().iterator().next();
//                    Range<ComparableNodeValue> firstB = br.asRanges().iterator().next();
//
//                    Range<ComparableNodeValue> lastA = ar.asDescendingSetOfRanges().iterator().next();
//                    Range<ComparableNodeValue> lastB = br.asDescendingSetOfRanges().iterator().next();
//
//                    AllenRelation ar1 = AllenRelations.compute(lastA, firstB);
//                    AllenRelation ar2 = AllenRelations.compute(lastB, firstA).invert();
//
//                    result = AllenRelation.union(result, ar1, ar2);
//                }
//            }
//        }
//
//        return result;
    }


    public static Range<Cmp<Entry<?, Cmp<ComparableNodeValue>>>> span(VSpace vspace) {
        Objects.requireNonNull(vspace);
        Comparator<Entry<?, Cmp<ComparableNodeValue>>> cmptor = (x, y) -> {
            // (Object x, ComparableNodeValue y)

            return ComparisonChain.start()
                    .compare(x.getKey(), y.getKey(), vspace.getDomain().getDimensionComparatorRaw())
                    .compare(x.getValue(), y.getValue())
                    .result();
        };

        CmpFactory<Entry<?, Cmp<ComparableNodeValue>>> dimCmpFactory = CmpFactory.of(cmptor);


        CmpFactory<ComparableNodeValue> cmpFactory = CmpFactory.of(ComparableNodeValue::compareTo);

        NodeRanges nr = ((VSpaceImpl)vspace).getNodeRanges();

        List<?> dimensionKeys;
        if (nr.isVscExhaustive()) {
            dimensionKeys = new ArrayList<>(nr.getValueSpaces());
            // Collections.sort(dimensionKeys, vspace.getDomain().getDimensionComparatorRaw());
        } else {
            dimensionKeys = new ArrayList<>(vspace.getDomain().getDimensions());
        }
        Collections.sort(dimensionKeys, vspace.getDomain().getDimensionComparatorRaw());


        Range<Cmp<Entry<?, Cmp<ComparableNodeValue>>>> totalSpan = null;
        for (Object dimensionKey : dimensionKeys) {
            RangeSet<ComparableNodeValue> ranges = nr.getDimension(dimensionKey);

            Range<ComparableNodeValue> spanContrib = ranges == null
                    ? Range.all()
                    : ranges.span();

            Endpoint<Cmp<ComparableNodeValue>> start = !spanContrib.hasLowerBound()
                    ? Endpoint.open(cmpFactory.minValue())
                    : Endpoint.of(cmpFactory.wrap(spanContrib.lowerEndpoint()), spanContrib.lowerBoundType());

            Endpoint<Cmp<ComparableNodeValue>> end = !spanContrib.hasUpperBound()
                    ? Endpoint.open(cmpFactory.maxValue())
                    : Endpoint.of(cmpFactory.wrap(spanContrib.upperEndpoint()), spanContrib.upperBoundType());

            Range<Cmp<ComparableNodeValue>> mappedRange = RangeUtils.create(start, end);

            Range<Cmp<Entry<?,Cmp<ComparableNodeValue>>>> dimRange = RangeUtils.map(mappedRange,
                    e -> dimCmpFactory.wrap(Map.entry(dimensionKey, e)));

            if (totalSpan == null) {
                totalSpan = dimRange;
            } else {
                totalSpan = totalSpan.span(dimRange);
            }
        }

        return totalSpan;
    }


//    public static <K extends Comparable<K>, V extends Comparable<V>> Range<Cmp<Entry<K, V>>> map(K key, Range<V> range) {
//
//    }


//    @Override
//    public VSpace moveDimension(Object fromDimKey, Object toDimKey) {
//        RangeSet<ComparableNodeValue> ranges = nodeRanges.getDimension(fromDimKey);
//        nodeRanges.removeDimension(fromDimKey); // .setDimension(fromDimKey, null);
//        nodeRanges.setDimension(toDimKey, ranges);
//        return this;
//    }
}
