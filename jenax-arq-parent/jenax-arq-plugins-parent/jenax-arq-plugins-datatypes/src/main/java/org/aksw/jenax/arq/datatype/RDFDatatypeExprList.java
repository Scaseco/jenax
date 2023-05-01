package org.aksw.jenax.arq.datatype;

import org.apache.jena.sparql.expr.E_Coalesce;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;

public class RDFDatatypeExprList { // Does not inherit from RDFDatatype (yet)

//    public static final String IRI = "http://jsa.aksw.org/dt/sparql/exprlist";
//    public static final RDFDatatypeExprList INSTANCE = new RDFDatatypeExprList();
//
//    public static RDFDatatype get() {
//        return INSTANCE;
//    }

    /** Unparse an expression list as a string */
    public static String unparse(ExprList el) {
        // We use an n-ary 'wrapper' expression to serialize the list of segments
        // FIXME exploiting coalesce is obviously a hack; use some custom function IRI
        Expr wrapper = new E_Coalesce(el);
        String result = ExprUtils.fmtSPARQL(wrapper);

        return result;
    }

    /** Parse a string as an arbitrary function and extract the arguments as an ExprList */
    public static ExprList parse(String str) {
        Expr expr = ExprUtils.parse(str);

        ExprFunction fn = (ExprFunction)expr;
        ExprList result = new ExprList();

        for (Expr e : fn.getArgs()) {
            result.add(e);
        }

        return result;
    }
}
