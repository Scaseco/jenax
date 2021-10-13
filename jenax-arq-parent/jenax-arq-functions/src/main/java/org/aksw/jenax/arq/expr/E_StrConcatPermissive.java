package org.aksw.jenax.arq.expr;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A 'permissive' version of Jena's E_StrConcat, that does not
 * complain when mixing types (e.g. concat(string, int)).
 *
 *
 * @author raven
 *
 */
public class E_StrConcatPermissive extends ExprFunctionN
{
    private static final String name = "concatPermissive" ;

    public E_StrConcatPermissive(ExprList args)
    {
        super(name, args) ;
    }

    public static E_StrConcatPermissive create(Expr ...exprs) {
        return new E_StrConcatPermissive(new ExprList(Arrays.asList(exprs)));
    }

    @Override
    public Expr copy(ExprList newArgs)
    {
        return new E_StrConcatPermissive(newArgs) ;
    }

    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        String str = "";
        for(NodeValue arg : args) {
            str += arg.asUnquotedString();
        }
        return NodeValue.makeString(str);
    }
}
