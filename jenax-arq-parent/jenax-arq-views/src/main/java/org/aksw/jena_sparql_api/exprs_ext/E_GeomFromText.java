package org.aksw.jena_sparql_api.exprs_ext;

import java.sql.SQLException;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.NodeValue;
import org.postgis.PGgeometry;


public class E_GeomFromText
    extends ExprFunction1
{
    private static final String symbol = "ST_GeomFromText" ;

    /*
    @Override
    public boolean isConstant() {
        return false;
    }*/

    public E_GeomFromText(Expr arg) {
        super(arg, symbol);
    }


    @Override
    public NodeValue eval(NodeValue v) {
        // TODO If we evaluate to an PGgeometry object, then the call to geomFormText will not be pushed.

        try {
            return new NodeValueGeom(new PGgeometry(v.getString()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Expr copy(Expr expr) {
        return new E_GeomFromText(expr);
    }
}
