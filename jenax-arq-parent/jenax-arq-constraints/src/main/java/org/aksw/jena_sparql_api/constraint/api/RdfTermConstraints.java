package org.aksw.jena_sparql_api.constraint.api;

import java.util.EnumSet;

import org.aksw.jenax.arq.rdfterm.RdfTermType;
import org.apache.jena.graph.Node;

//public class RdfTermConstraints {
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
//}
