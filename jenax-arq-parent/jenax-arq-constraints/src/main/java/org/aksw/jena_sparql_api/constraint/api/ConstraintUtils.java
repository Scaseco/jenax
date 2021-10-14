package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.TupleSlot;

public class ConstraintUtils {

    /** Derive constraints from a quad; merges them into the RowConstraints instance */
    private void deriveConstraints(ConstraintRow row, Quad quad) {

        for (int i = 0; i < 3; ++i) {
            Node node = QuadUtils.getNode(quad, i);

            // For variables we can derive the term type from the slot
            if (node.isVariable()) {
                Var var = (Var)node;
                // Get the appropriate constraints for the slot
                TupleSlot slot = QuadUtils.idxToSlot(i);


                RdfTermConstraints.stateSlot(row, var, slot);
            }
        }
    }

}
