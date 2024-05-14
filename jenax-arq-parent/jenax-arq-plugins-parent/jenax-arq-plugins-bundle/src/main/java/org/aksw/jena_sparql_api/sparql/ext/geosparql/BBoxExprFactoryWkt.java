package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.Geof;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public class BBoxExprFactoryWkt
    // implements BBoxExprFactory
{
    // protected Var wktVar;
    protected String intersectsFnName;

    // GeomFromTextFn is for Virtuoso - may be null for most other stores
    protected String geomFromTextFnName;

    public BBoxExprFactoryWkt(String intersectsFnName, String geomFromTextFnName) {
        // this.wktVar = wktVar;
        this.intersectsFnName = intersectsFnName;
        this.geomFromTextFnName = geomFromTextFnName;
    }

//    public static BBoxExprFactoryWkt of(Var wktVar) {
//        return of(wktVar, null, null);
//    }

    public static BBoxExprFactoryWkt ofGeoSparql() {
        return of(Geof.SF_INTERSECTS, null);
    }

    public static BBoxExprFactoryWkt of(String intersectsFnName, String geomFromTextFnName) {
        return new BBoxExprFactoryWkt(intersectsFnName, geomFromTextFnName);
    }

//    public Var getWktVar() {
//        return wktVar;
//    }

    public String getIntersectsFnName() {
        return intersectsFnName;
    }

    public String getGeomFromTextFnName() {
        return geomFromTextFnName;
    }

    // @Override
    public Expr createExpr(Node var, Envelope envelope) {
        Geometry geom = CustomGeometryFactory.theInstance().toGeometry(envelope);
        return createExpr(var, geom);
    }

    public Expr createExpr(Node var, Geometry geom) {
        GeometryWrapper geomWrapper = new GeometryWrapper(geom, Geo.WKT);
        return createExpr(var, geomWrapper);
    }

    public Expr createExpr(Node var, GeometryWrapper geom) {
        Node node = geom.asNode();
        return createExpr(var, node);
    }

    public Expr createExpr(Node var, Node geom) {
        Expr ev = ExprLib.nodeToExpr(var);
        NodeValue nv = NodeValue.makeNode(geom);
        return createExpr(ev, nv);
    }

    public Expr createExpr(Expr lhs, Expr geom) {
        return GeoExprUtils.createExprOgcIntersects(lhs, geom, intersectsFnName, geomFromTextFnName);
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
