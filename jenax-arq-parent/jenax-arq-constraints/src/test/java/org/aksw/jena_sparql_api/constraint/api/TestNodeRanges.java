package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jena_sparql_api.constraint.util.NodeRanges;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;


public class TestNodeRanges {

    /** Application of of node ranges for prefix matching */
    @Test
    public void testPrefixMatching() {
        NodeRanges nr = NodeRanges.create();

        nr.add(Range.closedOpen(
                NodeWrapper.wrap(NodeFactory.createLiteral("a")),
                NodeWrapper.wrap(NodeFactory.createLiteral("b"))));

        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("a")));
        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("ab")));
        Assert.assertFalse(nr.contains(NodeFactory.createLiteral("b")));
        Assert.assertFalse(nr.contains(NodeValue.makeInteger(1).asNode()));
    }
}

