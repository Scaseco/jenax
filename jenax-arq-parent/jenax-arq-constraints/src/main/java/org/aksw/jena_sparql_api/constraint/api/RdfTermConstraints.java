package org.aksw.jena_sparql_api.constraint.api;

import org.apache.jena.sparql.core.mem.TupleSlot;

public class RdfTermConstraints {

    public static boolean stateSlot(ConstraintRow row, TupleSlot slot) {
        return true;
    }

//
//    /** Create a restriction for the graph component; only allows for iris */
//    public static ConstraintImpl forGraph() {
//        return new ConstraintImpl(EnumSet.of(RdfTermType.IRI));
//    }
//
//    /** Create a restriction for the graph component; only allows for iris and bnodes */
//    public static ConstraintImpl forSubject() {
//        return new ConstraintImpl(EnumSet.of(RdfTermType.IRI, RdfTermType.BNODE));
//    }
//
//    /** Create a restriction for the graph component; only allows for iris */
//    public static ConstraintImpl forPredicate() {
//        return new ConstraintImpl(EnumSet.of(RdfTermType.IRI));
//    }
//
//    /** Create a restriction for the graph component; allows for iris, bnodes and literals */
//    public static ConstraintImpl forObject() {
//        return new ConstraintImpl(EnumSet.of(RdfTermType.IRI, RdfTermType.BNODE, RdfTermType.LITERAL));
//    }
//
//
//    /** Create a restriction for a given node */
//    public static ConstraintImpl forNode(Node node) {
//        ConstraintImpl result = forObject();
//        result.stateNode(node);
//        return result;
//    }
//
//
}
