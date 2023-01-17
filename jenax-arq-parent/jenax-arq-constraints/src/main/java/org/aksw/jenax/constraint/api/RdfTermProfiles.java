package org.aksw.jenax.constraint.api;

import org.aksw.jenax.constraint.impl.ValueSpaceImpl;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.TupleSlot;

public class RdfTermProfiles {

    public static boolean stateSlot(ConstraintRow row, TupleSlot slot) {
        return true;
    }

    /** Unconstrained */
    public static ValueSpace newOpenProfile() {
        NodeRanges nr = NodeRanges.createOpen();
        return ValueSpaceImpl.create(nr);
    }

    /** Only open the IRI value space*/
    public static ValueSpace newIriProfile() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_URI);
        return ValueSpaceImpl.create(nr);
    }

    /** Open all spaces other than IRI, BNODE and TRIPLE */
    public static ValueSpace newLiteralProfile() {
        NodeRanges nr = NodeRanges.createOpen();
        nr.addEmptyDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_URI);
        nr.addEmptyDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_BLANKNODE);
        nr.addEmptyDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_QUOTED_TRIPLE);
        return ValueSpaceImpl.create(nr);
    }

    /** Open all spaces other than IRI, BNODE and TRIPLE */
    public static ValueSpace newNonLiteralProfile() {
        NodeRanges nr = NodeRanges.createClosed();
        nr.addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_URI);
        nr.addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_BLANKNODE);
        nr.addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_QUOTED_TRIPLE);
        return ValueSpaceImpl.create(nr);
    }


    /** Create a restriction for the graph component; only allows for iris */
    public static ValueSpace forGraph() {
        return newIriProfile();
    }

    /** Create a restriction for the graph component; only allows for iris and bnodes */
    public static ValueSpace forSubject() {
        return newNonLiteralProfile();
    }

    /** Create a restriction for the graph component; only allows for iris */
    public static ValueSpace forPredicate() {
        return newIriProfile();
    }

    /** Create a restriction for the graph component; allows for anything */
    public static ValueSpace forObject() {
        return newOpenProfile();
    }


    /** Create a restriction for a given node */
    public static ValueSpace forNode(Node node) {
        NodeRanges nr = NodeRanges.createOpen();
        nr.stateValue(node);
        return ValueSpaceImpl.create(nr);
    }


    public static ValueSpace forSlot(TupleSlot slot) {
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
