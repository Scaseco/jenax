package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.List;

import org.aksw.commons.util.list.ListUtils;
import org.aksw.commons.util.obj.ObjectUtils;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.sparql.expr.E_StrSubstring;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Fixes substr(str, start, len) of virtuoso which does not
 * restrict the substring to the length of the string.
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
            Expr offset = ListUtils.getOrNull(argList, 1);
            Expr len = ListUtils.getOrNull(argList, 2);

            // if (?offset >= strlen(?str), "",
            //     substr(?str, ?offset,
            //         if (?len >= strlen(?str) - ?offset, ?strlen(?str) - ?len, ?len)))
            Expr strLen = new E_StrLength(str);

            // TODO Negative values for offsets and length not handled yet
            result = new E_Conditional(new E_GreaterThanOrEqual(offset, strLen), NodeValue.makeString(""),
                    new E_StrSubstring(str, offset, new E_Conditional(new E_GreaterThanOrEqual(len, new E_Subtract(strLen, offset)),
                            new E_Subtract(strLen, offset), len)));
        } else {
            result = super.transform(func, args);
        }
        return result;
    }
}
