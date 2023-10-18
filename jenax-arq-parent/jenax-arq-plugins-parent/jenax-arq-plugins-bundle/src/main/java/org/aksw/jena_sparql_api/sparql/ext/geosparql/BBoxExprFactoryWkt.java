package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.locationtech.jts.geom.Envelope;

public class BBoxExprFactoryWkt
    implements BBoxExprFactory
{
    protected Var wktVar;
    protected String intersectsFnName;
    protected String geomFromTextFnName;

    public BBoxExprFactoryWkt(Var wktVar, String intersectsFnName, String geomFromTextFnName) {
        this.wktVar = wktVar;
        this.intersectsFnName = intersectsFnName;
        this.geomFromTextFnName = geomFromTextFnName;
    }

    public static BBoxExprFactoryWkt of(Var wktVar) {
        return of(wktVar, null, null);
    }

    public static BBoxExprFactoryWkt of(Var wktVar, String intersectsFnName, String geomFromTextFnName) {
        return new BBoxExprFactoryWkt(wktVar, intersectsFnName, geomFromTextFnName);
    }

    public Var getWktVar() {
        return wktVar;
    }

    public String getIntersectsFnName() {
        return intersectsFnName;
    }

    public String getGeomFromTextFnName() {
        return geomFromTextFnName;
    }

    @Override
    public Expr createExpr(Envelope bbox) {
        Expr result = GeoExprUtils.createExprOgcIntersects(wktVar, bbox, intersectsFnName, geomFromTextFnName);
        return result;
    }

//    createExpr: function(bounds) {
//        var result = GeoExprUtils.createExprOgcIntersects(this.wktVar,bounds, this.intersectsFnName, this.geomFromTextFnName);
//        return result;
//    }
//
//    public static BBoxExprFactoryWkt of(Var xVar, Var yVar, Node castNode) {
//    	return new BBoxExprFactoryWkt(xVar, yVar, castNode);
//    }

}
