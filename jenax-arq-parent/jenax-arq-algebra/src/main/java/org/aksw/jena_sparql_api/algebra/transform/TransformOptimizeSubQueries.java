package org.aksw.jena_sparql_api.algebra.transform;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.util.obj.ObjectUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;

/**
 * Note: TransformRedundantProjectionRemoval seems to be the better choice.
 * Shifting OpSlice on Virtuoso may break queries...
 *
 * Attempted fix for the following query on Virtuoso version 07.20.3217:
 * <pre>
 * SELECT  (if(isBlank(?Graph_1), URI(concat("bnode://", str(?Graph_1))), ?Graph_1) AS ?Graph)
 * WHERE
 *  { SELECT  ?Graph_1
 *    WHERE
 *      { SELECT DISTINCT  ?Graph_1
 *        WHERE
 *          { GRAPH ?Graph_1
 *              { ?__Graph_1_s  ?__Graph_1_p  ?__Graph_1_o }
 *          }
 *      }
 *    LIMIT   1
 *  }
 * </pre>
 *
 * Try to avoid sub-queries if possible by:
 * <ul>
 *   <li>Remove needless OpProject. This is the case when only the visible variables projected.</li>
 *   <li>Pull OpSlice over OpProject</li>
 *   <li>Pull OpSlice over OpExtend</li>
 * <ul>
 *
 * Note: This transform pulls up Slice and may thus conflicts with
 * TransformPushSlice which attemps to push slice into SERVICE!
 */
public class TransformOptimizeSubQueries
    extends TransformCopy
{
    @Override
    public Op transform(OpProject op, Op subOp) {
        Op result = null;

        // Omit needless opProject - e.g. OpProject(?x, OpSlice(10-20, OpDistinct(OpProject(?x, subOp))))
        Set<Var> subOpVars = OpVars.visibleVars(subOp);

        Set<Var> thisVars = new HashSet<>(op.getVars());
        boolean isNeedless = thisVars.equals(subOpVars);

        if (isNeedless) {
            result = subOp;
        } else {
            // OpProject(OpSlice()) -> OpSlice(OpProject())
            OpSlice slice = ObjectUtils.castAsOrNull(OpSlice.class, subOp);
            if (slice != null) {
                result = new OpSlice(
                    new OpProject(slice.getSubOp(), op.getVars()),
                    slice.getStart(), slice.getLength());
            }
        }

        if (result == null) {
            result = super.transform(op, subOp);
        }
        return result;
    }

    @Override
    public Op transform(OpExtend op, Op subOp) {
        Op result = null;
        OpSlice slice = ObjectUtils.castAsOrNull(OpSlice.class, subOp);
        if (slice != null) {
            result = new OpSlice(
                OpExtend.create(slice.getSubOp(), new VarExprList(op.getVarExprList())),
                slice.getStart(), slice.getLength());
        }

        if (result == null) {
            result = super.transform(op, subOp);
        }
        return result;
    }
}
