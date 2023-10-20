package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Arrays;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Envelope;

/** Variable (not vocabulary) based utilities to create filter expressions for geo data */
public class GeoExprUtils {
    /**
     * @param varX The SPARQL variable that corresponds to the longitude
     * @param varY The SPARQL variable that corresponds to the longitude
     * @param bounds The bounding box to use for filtering
     * @param castNode An optional SPAQRL node used for casting, e.g. xsd.xdouble
     */
    public static Expr createExprWgs84Intersects(Var varX, Var varY, Envelope bounds, Node castNode) {
        Expr lon = new ExprVar(varX);
        Expr lat = new ExprVar(varY);

        // Cast the variables if requested
        // Using E_Function(castNode.getUri(), lon) - i.e. the cast type equals the cast function name
        if(castNode != null) {
            String fnName = castNode.getURI();
            lon = new E_Function(fnName, new ExprList(lon));
            lat = new E_Function(fnName, new ExprList(lat));
        }
        NodeValue xMin = NodeValue.makeDecimal(bounds.getMinX());
        NodeValue xMax = NodeValue.makeDecimal(bounds.getMaxX());
        NodeValue yMin = NodeValue.makeDecimal(bounds.getMinY());
        NodeValue yMax = NodeValue.makeDecimal(bounds.getMaxY());

        Expr result = new E_LogicalAnd(
            new E_LogicalAnd(new E_GreaterThanOrEqual(lon, xMin), new E_LessThan(lon, xMax)),
            new E_LogicalAnd(new E_GreaterThanOrEqual(lat, yMin), new E_LessThan(lat, yMax))
        );

        return result;
    }

    public static Expr createExprOgcIntersects(Var v, Envelope bounds, String intersectsFnName, String geomFromTextFnName) {
        var ogc = "http://www.opengis.net/rdf#";

        intersectsFnName = Optional.ofNullable(intersectsFnName).orElse(ogc + "intersects");
        geomFromTextFnName = Optional.ofNullable(geomFromTextFnName).orElse(ogc + "geomFromText");

        ExprVar exprVar = new ExprVar(v);
        String wktStr = boundsToWkt(bounds);

        // FIXME: Better use typeLit with xsd:string
        NodeValue wktNodeValue = NodeValue.makeString(wktStr); //new NodeValue(rdf.NodeFactory.createPlainLiteral(wktStr));

        Expr result = new E_Function(
            intersectsFnName,
            new ExprList(Arrays.asList(exprVar, new E_Function(geomFromTextFnName, new ExprList(wktNodeValue)))));

        return result;
    }

    /**
     * Convert a bounds object to a WKT polygon string
     *
     * TODO This method could be moved to a better place
     *
     */
    public static String boundsToWkt(Envelope bounds) {
        // Should use the WKT writer
        // new WKTWriter().write(new GeometryWrapper(bounds, GeoSPARQL_URI));
        // WKTWriter.write(null)
        double ax = bounds.getMinX();
        double ay = bounds.getMaxY();
        double bx = bounds.getMaxX();
        double by = bounds.getMinY(); // is min and max correct for y?

        var result = "POLYGON((" + ax + " " + ay + "," + bx + " " + ay
                + "," + bx + " " + by + "," + ax + " " + by + "," + ax
                + " " + ay + "))";

        return result;
    }
}
