package org.aksw.jenax.constraint.impl;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.expr.ValueSpace;

import com.google.common.collect.Range;

public class RdfTermProfiles {

//    public static boolean stateSlot(CBinding row, TupleSlot slot) {
//        return true;
//    }

    /** Only open the IRI value space*/
    public static VSpace newUnboundProfile() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.addOpenDimension(ValueSpace.VSPACE_UNDEF);
        return VSpaceImpl.create(nr);
    }

    /** Unconstrained */
    public static VSpace newOpenProfile() {
        NodeRanges nr = NodeRanges.createOpen();
        return VSpaceImpl.create(nr);
    }

    /** Only open the IRI value space*/
    public static VSpace newIriProfile() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.addOpenDimension(ValueSpace.VSPACE_URI);
        return VSpaceImpl.create(nr);
    }

    /** Open all spaces other than IRI, BNODE and TRIPLE */
    public static VSpace newLiteralProfile() {
        NodeRanges nr = NodeRanges.createOpen();
        nr.addEmptyDimension(ValueSpace.VSPACE_URI);
        nr.addEmptyDimension(ValueSpace.VSPACE_BLANKNODE);
        nr.addEmptyDimension(ValueSpace.VSPACE_TRIPLE_TERM);
        return VSpaceImpl.create(nr);
    }

    /** Open all spaces other than IRI, BNODE and TRIPLE */
    public static VSpace newNonLiteralProfile() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.addOpenDimension(ValueSpace.VSPACE_URI);
        nr.addOpenDimension(ValueSpace.VSPACE_BLANKNODE);
        nr.addOpenDimension(ValueSpace.VSPACE_TRIPLE_TERM);
        return VSpaceImpl.create(nr);
    }


    /** Create a restriction for the graph component; only allows for iris */
    public static VSpace forGraph() {
        return newIriProfile();
    }

    /** Create a restriction for the graph component; only allows for iris and bnodes */
    public static VSpace forSubject() {
        return newNonLiteralProfile();
    }

    /** Create a restriction for the graph component; only allows for iris */
    public static VSpace forPredicate() {
        return newIriProfile();
    }

    /** Create a restriction for the graph component; allows for anything */
    public static VSpace forObject() {
        return newOpenProfile();
    }


    /** Create a restriction for a given node */
    public static VSpace forNode(Node node) {
        NodeRanges nr = NodeRanges.createOpen();
        nr.stateValue(node);
        return VSpaceImpl.create(nr);
    }

    public static VSpace forStringPrefix(String prefix) {
        NodeRanges nr = NodeRanges.createClosed();
        nr.add(NodeRanges.rangeForStringPrefix(prefix));
        return VSpaceImpl.create(nr);
    }


    public static VSpace forIriPrefix(String prefix) {
        NodeRanges nr = NodeRanges.createClosed();
        Range<ComparableNodeValue> range = RangeUtils.map(NodeRanges.rangeForStringPrefix(prefix), x -> ComparableNodeValue.wrap(NodeFactory.createURI(x.getNodeValue().getString())));
        nr.add(range);
        return VSpaceImpl.create(nr);
    }


    public static VSpace forSlot(TupleSlot slot) {
        switch (slot) {
        case GRAPH: return forGraph();
        case SUBJECT: return forSubject();
        case PREDICATE: return forPredicate();
        case OBJECT: return forObject();
        default:
            throw new IllegalArgumentException("Unsupported slot: " + slot);
        }
    }

}
