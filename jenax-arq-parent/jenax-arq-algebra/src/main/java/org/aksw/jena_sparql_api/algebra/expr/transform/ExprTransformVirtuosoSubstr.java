package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.List;

import org.aksw.commons.util.list.ListUtils;
import org.aksw.commons.util.obj.ObjectUtils;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.sparql.expr.E_StrSubstring;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Fixes substr(str, start, len) of virtuoso which raises an error if the
 * requested substring is longer than the string.
 *
 * Note: This rewrite does not handle the cases where start < 1 or len < 0
 */
public class ExprTransformVirtuosoSubstr
    extends ExprTransformCopy
{
    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        Expr result;
        E_StrSubstring e = ObjectUtils.castAsOrNull(E_StrSubstring.class, func);
        if (e != null) {
            List<Expr> argList = args.getList();
            Expr str = ListUtils.getOrNull(argList, 0);
            Expr start = ListUtils.getOrNull(argList, 1);
            Expr len = ListUtils.getOrNull(argList, 2);

            // If we wanted to handle offsets < 1 we'd need:
            // Adjusted offset: if (?offset < 1 ? 1 : ?offset)
            // shift: if (?offset >= 1 ? 0 : 1 - ?offset)

            Expr strLen = new E_StrLength(str);

            // zero-based-start = (?start - 1)
            // remaining length = strlen(?str) - zero-based-start
            Expr start0 = new E_Subtract(start, NodeValue.nvONE);
            Expr remainingLen = new E_Subtract(strLen, start0);

            if (len == null) {
                // if (?start > strlen(?str), "",
                //     substr(?str, ?start, ?remaining)))

                result = new E_Conditional(new E_GreaterThan(start, strLen), NodeValue.makeString(""),
                        new E_StrSubstring(str, start, remainingLen));

            } else {
                // if (?offset > strlen(?str), "",
                //     substr(?str, ?start,
                //         if (?len > ?remaining, ?remaining, ?len)))

                result = new E_Conditional(new E_GreaterThan(start, strLen), NodeValue.makeString(""),
                        new E_StrSubstring(str, start, new E_Conditional(new E_GreaterThan(len, remainingLen),
                                remainingLen, len)));
            }

            // Constant-folding should be done extrinsically
            // result = ExprTransformer.transform(new ExprTransformConstantFold(), result);

        } else {
            result = super.transform(func, args);
        }
        return result;
    }
}
