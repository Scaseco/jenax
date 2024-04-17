package org.aksw.jena_sparql_api.algebra.transform;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

public class TransformRedundantProjectionRemoval
    extends TransformCopy
{
    @Override
    public Op transform(OpProject op, Op subOp) {
        // Omit needless opProject - e.g. OpProject(?x, OpSlice(10-20, OpDistinct(OpProject(?x, subOp))))
        Set<Var> subOpVars = OpVars.visibleVars(subOp);

        Set<Var> thisVars = new HashSet<>(op.getVars());
        boolean isNeedless = thisVars.equals(subOpVars);
        Op result = isNeedless
                ? subOp
                : super.transform(op, subOp);
        return result;
    }
}
