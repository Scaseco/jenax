package org.aksw.jenax.constraint.index;

import java.util.Map;

import org.aksw.jenax.constraint.util.NodeWrapper;

import com.google.common.collect.RangeMap;

public class NodeRangeIndex {
    Map<Object, RangeMap<NodeWrapper, Integer>> spaceToRanges;
}
