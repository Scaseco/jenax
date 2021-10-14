package org.aksw.jenax.constraint.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.sparql.expr.ValueSpaceClassification;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public abstract class ValueSpaceBase<T extends Comparable<T>, D> {
    protected Map<D, RangeSet<T>> vscToRangeSets = new HashMap<>();
    protected boolean isVscExhaustive;

    protected abstract D classifyValueSpace(Range<T> range);


    /** Add a new empty dimension. Do nothing if it already exists or if the dimensions are exhaustive */
    public void addEmptyDimension(D dimension) {
        if (!isVscExhaustive) {
            vscToRangeSets.computeIfAbsent(dimension, x -> TreeRangeSet.create());
        }
    }

    /** Add a new unconstrained dimension. Do nothing if it already exists or dimensions are non-exhaustive*/
    public void addOpenDimension(D dimension) {
        if (isVscExhaustive) {
            vscToRangeSets.computeIfAbsent(dimension, x -> TreeRangeSet.<T>create().complement());
        }
    }


    /** Unconstrained mode means that any valid srange is considered enclosed by this one */
    public boolean isUnconstrained() {
        return !isVscExhaustive && vscToRangeSets.isEmpty();
    }

    // @Override
    public boolean isConflicting() {
        return isVscExhaustive && vscToRangeSets.isEmpty();
    }

    public void add(Range<T> range) {
        D vsc = classifyValueSpace(range);

        if (ValueSpaceClassification.VSPACE_DIFFERENT.equals(vsc) && vscToRangeSets != null) {
            // Inconsistent range - go into conflicting state
            vscToRangeSets.clear();
            this.isVscExhaustive = true;
        } else {
            RangeSet<T> rangeSet = vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());
            rangeSet.add(range);
        }
    }


    public void substract(Range<T> range) {

        D vsc = classifyValueSpace(range);

        if (vsc == null) {
            // null means substracting all possible ranges
            vscToRangeSets.clear();
        } else if (ValueSpaceClassification.VSPACE_DIFFERENT.equals(vsc) && vscToRangeSets != null) {
            // substracting a contradicting range does nothing
            // raise exception?
        } else {
            RangeSet<T> rangeSet = vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());

//                RangeSet<NodeWrapper> tmp = TreeRangeSet.create();
//                tmp.add(range);
//                RangeSet<NodeWrapper> substraction = tmp.complement();

            rangeSet.remove(range);
        }
    }

    /**
     * Mutate the ranges of this to create the intersection with other
     *
     * @param that
     * @return
     */
    public ValueSpaceBase<T, D> stateIntersection(ValueSpaceBase<T, D> that) {

        if (!this.isVscExhaustive) {
            for (Iterator<Entry<D, RangeSet<T>>> it = that.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
                Entry<D, RangeSet<T>> e = it.next();
                D vsc = e.getKey();
                RangeSet<T> thatRangeSet = e.getValue();

                if (!this.vscToRangeSets.containsKey(vsc)) {
                    this.vscToRangeSets.put(vsc, thatRangeSet);
                }
            }
        }

        //for (Entry<Object, RangeSet<ComparableNodeWrapper>> e: vscToRangeSets.entrySet()) {
        for (Iterator<Entry<D, RangeSet<T>>> it = this.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
            Entry<D, RangeSet<T>> e = it.next();
            Object vsc = e.getKey();
            RangeSet<T> thisRangeSet = e.getValue();

            RangeSet<T> thatRangeSet = that.vscToRangeSets.get(vsc);

            // If there are no other ranges in the value space than the intersection is empty
            if (thatRangeSet == null) {
                if (that.isVscExhaustive) {
                    it.remove();
                } else {
                    // noop
                }
            } else {
                // Intersection by means of removing the other's complement
                // https://github.com/google/guava/issues/1825
                RangeSet<T> thatComplement = thatRangeSet.complement();
                thisRangeSet.removeAll(thatComplement);

                if (thisRangeSet.isEmpty()) {
                    it.remove();
                }
            }
        }

        this.isVscExhaustive = this.isVscExhaustive || that.isVscExhaustive;

        return this;
    }


    /**
     * Add all other ranges to this one
     *
     * @param that
     * @return
     */
    public ValueSpaceBase<T, D> stateUnion(ValueSpaceBase<T, D> that) {
        this.isVscExhaustive = this.isVscExhaustive && that.isVscExhaustive;

        for (Iterator<Entry<D, RangeSet<T>>> it = that.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
            Entry<D, RangeSet<T>> e = it.next();
            D vsc = e.getKey();

            RangeSet<T> thatRangeSet = e.getValue();
            RangeSet<T> thisRangeSet = this.vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());

            thisRangeSet.addAll(thatRangeSet);

            // In open mode (= not exhaustive) remove all unconstrained ranges
            if (!this.isVscExhaustive && thisRangeSet.complement().isEmpty()) {
                it.remove();
            }
        }

        return this;
    }

}
