package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpExtend;

/**
 * Transform OpAssign to OpExtend.
 * Rationale: Execution of OpLateral in Jena 5.0.0 inserts OpAssign operations. Attempting to execute those remote results in
 * SPARQL LET syntax elements which are usually not understood by remote endpoints.
 */
public class TransformAssignToExtend
    extends TransformCopy
{
    private static TransformAssignToExtend INSTANCE = null;

    public static TransformAssignToExtend get() {
        if (INSTANCE == null) {
            synchronized (TransformAssignToExtend.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TransformAssignToExtend();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        return OpExtend.create(subOp, opAssign.getVarExprList());
    }
}
