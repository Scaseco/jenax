package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jena_sparql_api.constraint.util.NodeRanges;
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
                NodeWrapper.wrap(NodeFactory.createLiteral("a")),
                NodeWrapper.wrap(NodeFactory.createLiteral("b"))));

        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("a")));
        Assert.assertTrue(nr.contains(NodeFactory.createLiteral("ab")));
        Assert.assertFalse(nr.contains(NodeFactory.createLiteral("b")));
        Assert.assertFalse(nr.contains(NodeValue.makeInteger(1).asNode()));
    }


    @Test
    public void testPrefixRanges2() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.add(Range.closedOpen(
                NodeWrapper.wrap(NodeFactory.createLiteral(RDF.uri)),
                NodeWrapper.wrap(NodeFactory.createLiteral(incrementLastCharacter(RDF.uri)))));

//        NodeRanges nr2 = NodeRanges.create();
//        nr2.add(Range.singleton(NodeWrapper.wrap(NodeFactory.createLiteral(RDF.type.getURI()))));
//        System.out.println(nr2);
//        nr.stateIntersection(nr2);
        nr.substract(Range.singleton(NodeWrapper.wrap(NodeFactory.createLiteral(RDF.type.getURI()))));


        System.out.println(nr);
    }


    /**
     * Increment the last character (todo: should be byte?) of a string.
     * Useful for defining the upper bound of a range of strings with a certain prefix.
     */
    public static String incrementLastCharacter(String str) {
        int i = str.length() - 1;

        String result;
        if (i < 0) {
            result = str;
        } else {
            char lastChar = str.charAt(i);
            char nextChar = (char)(lastChar + 1);
            result = str.substring(0, i) + nextChar;
        }

        return result;
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

        Assert.assertTrue(notFive.stateIntersection(five).isContradicting());

    }

}

