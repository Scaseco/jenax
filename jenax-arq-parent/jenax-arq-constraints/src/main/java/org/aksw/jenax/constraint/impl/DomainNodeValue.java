package org.aksw.jenax.constraint.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.Domain;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.sparql.expr.ValueSpace;

import com.google.common.collect.Range;


public class DomainNodeValue
    implements Domain<ValueSpace, ComparableNodeValue> {

    private static final DomainNodeValue INSTANCE = new DomainNodeValue();

    public static final DomainNodeValue get() {
        return INSTANCE;
    }

    protected Set<ValueSpace> dimensions = new LinkedHashSet<>(Arrays.asList(ValueSpace.values()));

    @Override
    public Set<ValueSpace> getDimensions() {
        return dimensions;
    }

    @Override
    public Comparator<ValueSpace> getDimensionComparator() {
        return ValueSpace::comparisonOrder;
    }

    @Override
    public ValueSpace classify(Range<ComparableNodeValue> range) {
        ValueSpace result = NodeRanges.classifyValueSpaceCore(range);
        return result;
    }

    @Override
    public VSpace newOpenSpace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VSpace newClosedSpace() {
        // TODO Auto-generated method stub
        return null;
    }
}
