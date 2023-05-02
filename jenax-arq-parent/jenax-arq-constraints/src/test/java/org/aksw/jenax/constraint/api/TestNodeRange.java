package org.aksw.jenax.constraint.api;

import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;


public class TestNodeRange {

    /** Application of of node ranges for prefix matching */
    @Test
    public void testPrefixMatching() {
        NodeRanges nr = NodeRanges.createClosed();

        nr.add(Range.closedOpen(
                ComparableNodeValue.wrap(NodeFactory.createLiteral("a")),
                ComparableNodeValue.wrap(NodeFactory.createLiteral("b"))));

        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("a")));
        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("ab")));
        Assert.assertFalse(nr.contains(NodeFactory.createLiteral("b")));
        Assert.assertFalse(nr.contains(NodeValue.makeInteger(1).asNode()));
    }


    @Test
    public void testPrefixRanges2() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.add(Range.closedOpen(
                ComparableNodeValue.wrap(NodeFactory.createLiteral(RDF.uri)),
                ComparableNodeValue.wrap(NodeFactory.createLiteral(NodeRanges.incrementLastCharacter(RDF.uri)))));

//        NodeRanges nr2 = NodeRanges.create();
//        nr2.add(Range.singleton(NodeWrapper.wrap(NodeFactory.createLiteral(RDF.type.getURI()))));
//        System.out.println(nr2);
//        nr.stateIntersection(nr2);
        nr.substract(Range.singleton(ComparableNodeValue.wrap(NodeFactory.createLiteral(RDF.type.getURI()))));


        System.out.println(nr);
    }

    @Test
    public void testNegation() {
        NodeRanges notFive = NodeRanges.createOpen();
        notFive.substractValue(NodeValue.makeInteger(5).asNode());

        Assert.assertTrue(notFive.contains(NodeFactory.createLiteral("a")));
        Assert.assertTrue(notFive.contains(NodeFactory.createLiteral("ab")));
        Assert.assertFalse(notFive.contains(NodeValue.makeInteger(5).asNode()));


        NodeRanges five = NodeRanges.createOpen();
        five.stateValue(NodeValue.makeDouble(5).asNode());

        Assert.assertTrue(notFive.stateIntersection(five).isConflicting());

    }

}

