package org.aksw.jenax.constraint.util;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.impl.VSpaceImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class NodeSpaceFactory {
    public VSpace equalsTo(Node node) {
        VSpace result = VSpaceImpl.create(NodeRanges.createOpen().stateValue(node));
        return result;
    }

    /**
     * Return a vspace that does not equal the node - in the vspace of that node.
     * For example, notEquals(5) only matches _numeric_ values not equal to 5
     * It won't strings or IRIs.
     */
    public VSpace notEqualsTo(Node node) {
        VSpace result = VSpaceImpl.create(NodeRanges.createOpen().stateValue(node));
        return result;
    }

    public VSpace forStringPrefix(String str) {
        VSpace result = VSpaceImpl.create(NodeRanges.nodeRangesForPrefix(str));
        return result;
    }

    public VSpace range(Node lower, BoundType lowerBoundType, Node upper, BoundType upperBoundType) {
        NodeValue l = lower == null ? null : NodeValue.makeNode(lower);
        NodeValue u = upper == null ? null : NodeValue.makeNode(upper);
        VSpace result = range(l, lowerBoundType, u, upperBoundType);
        return result;
    }

    public VSpace range(NodeValue lower, BoundType lowerBoundType, NodeValue upper, BoundType upperBoundType) {
        ComparableNodeValue l = lower == null ? null : ComparableNodeValue.wrap(lower);
        ComparableNodeValue u = upper == null ? null : ComparableNodeValue.wrap(upper);
        Range<ComparableNodeValue> range = RangeUtils.create(l, lowerBoundType, u, upperBoundType);
        VSpace result = range(range);
        return result;
    }

    public VSpace range(Range<ComparableNodeValue> range) {
        NodeRanges nodeRanges = NodeRanges.createClosed();
        nodeRanges.add(range);
        VSpace result = VSpaceImpl.create(nodeRanges);
        return result;
    }
}
