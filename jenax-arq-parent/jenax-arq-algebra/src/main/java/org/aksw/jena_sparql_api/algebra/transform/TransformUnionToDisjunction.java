package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpUnion;

public class TransformUnionToDisjunction
    extends TransformCopy
{
    public static final TransformUnionToDisjunction fn = new TransformUnionToDisjunction();

    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        OpDisjunction result = OpDisjunction.create();
        add(result, left);
        add(result, right);
        return result;
    }

    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) {
        OpDisjunction result = OpDisjunction.create();
        for (Op elt : elts) {
            add(result, elt);
        }
        return result;
    }

    /**
     * This method does NOT recursively collect.
     * It only adds the immediate children because the transformer operates bottom up
     */
    public static void add(OpDisjunction dest, Op op) {
        if(op instanceof OpDisjunction) {
            OpDisjunction o = (OpDisjunction)op;
            for(Op subOp : o.getElements()) {
                dest.add(subOp);
            }
        } else if(op instanceof OpUnion) {
            OpUnion o = (OpUnion)op;
            dest.add(o.getLeft());
            dest.add(o.getRight());
        } else {
            dest.add(op);
        }
    }
}
