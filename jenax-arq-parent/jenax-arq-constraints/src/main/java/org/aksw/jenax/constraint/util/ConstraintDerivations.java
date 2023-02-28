package org.aksw.jenax.constraint.util;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.api.RdfTermProfiles;
import org.aksw.jenax.constraint.api.ValueSpace;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.expr.Expr;

public class ConstraintDerivations {

    /** Derive constraints from a quad; merges them into the RowConstraints instance */
    public static void deriveConstraints(ConstraintRow row, Quad quad) {

        for (int i = 0; i < 3; ++i) {
            Node node = QuadUtils.getNode(quad, i);

            // For variables we can derive the term type from the slot
            if (node.isVariable()) {
                Var var = (Var)node;
                // Get the appropriate constraints for the slot
                TupleSlot slot = QuadUtils.idxToSlot(i);

                ValueSpace vs = RdfTermProfiles.forSlot(slot);
                row.stateIntersection(var, vs);
            }
        }
    }

    public static void deriveConstraints(ConstraintRow row, Triple triple) {

        for (int i = 0; i < 3; ++i) {
            Node node = TripleUtils.getNode(triple, i);

            // For variables we can derive the term type from the slot
            if (node.isVariable()) {
                Var var = (Var)node;
                // Get the appropriate constraints for the slot
                TupleSlot slot = TripleUtils.idxToSlot(i);

                ValueSpace vs = RdfTermProfiles.forSlot(slot);
                row.stateIntersection(var, vs);
            }
        }
    }

    /** ElementBind */
    public static void deriveConstraints(ConstraintRow row, VarExprList defs) {

    }

    public static void derive(ConstraintRow row, Expr expr) {
        // TODO We need expr utils to extract op(var, constant) expressions - but they are in a higher module...
        // One possible solution is to define an interface at the lower level and provide the implementation in a higher one


        // TODO Recognize term type functions:
        // IRI(x), BNODE(y), STRDT(?, constantDatatype),
        //
        // TODO Recognize IRI(concat("const"))

    }

}
