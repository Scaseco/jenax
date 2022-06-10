package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.InitGeoSPARQL;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import com.google.common.math.DoubleMath;


public class TestGeoSparqlEx {

    private static final double TOLERANCE = 0.001;

    @Test
    public void testSimplify() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());

        NodeValue nv = ExprUtils.eval(ExprUtils.parse("geof:simplifyDp('POINT (0 0)'^^geo:wktLiteral)", pm));
        // System.out.println(nv);
        Assert.assertNotNull(nv);
    }

    @Test
    public void testLatLonViaExpr() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());

        double lon = ExprUtils.eval(ExprUtils.parse("geof:lon('POINT (89 179)'^^geo:wktLiteral)", pm)).getDouble();
        Assert.assertTrue(DoubleMath.fuzzyEquals(89, lon, TOLERANCE));

        double lat = ExprUtils.eval(ExprUtils.parse("geof:lat('POINT (89 179)'^^geo:wktLiteral)", pm)).getDouble();
        Assert.assertTrue(DoubleMath.fuzzyEquals(179, lat, TOLERANCE));
    }

    @Test
    public void testLatLonViaGeometryWrapper() {
        // In SRS_URI.WGS84_CRS it holds that lat=x and lon=y
        GeometryWrapper gw = GeometryWrapper.fromPoint(179, 89, SRS_URI.WGS84_CRS);

        double lon = GeoSparqlExFunctions.lon(gw);
        Assert.assertTrue(DoubleMath.fuzzyEquals(89, lon, TOLERANCE));

        double lat = GeoSparqlExFunctions.lat(gw);
        Assert.assertTrue(DoubleMath.fuzzyEquals(179, lat, TOLERANCE));
    }

    @Test
    public void testCentroidViaExpr() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());


        // rectangle with width 4 and height 2 - centroid expected at (2, 1)
        GeometryWrapper gw = GeometryWrapper.extract(
                ExprUtils.eval(ExprUtils.parse("geof:centroid('POLYGON((0 0, 4 0, 4 2, 0 2, 0 0))'^^geo:wktLiteral)", pm)));
        Assert.assertTrue(DoubleMath.fuzzyEquals(2, GeoSparqlExFunctions.x(gw.getParsingGeometry()), TOLERANCE));
        Assert.assertTrue(DoubleMath.fuzzyEquals(1, GeoSparqlExFunctions.y(gw.getParsingGeometry()), TOLERANCE));

//		System.out.println(gw);
    }

    @Test
    public void testAsGeoJSON() throws ParseException {
        new InitGeoSPARQL().start();

        String queryStr =
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#> " +
                        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/> " +
                        "SELECT ?x ?g { BIND('<gml:LineString xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"EPSG:25832\">" +
                        "<gml:posList>" +
                        "663957.75944074022118 5103981.64908889029175 663955.915655555087142 5103991.151674075052142" +
                        "</gml:posList>" +
                        "</gml:LineString>'^^geo:gmlLiteral as ?x) BIND(geof:asGeoJSON(?x) AS ?g) }";
        String[] tmpActual = {null};
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("gml", "http://www.opengis.net/ont/gml");
        QueryExecutionFactory.create(queryStr, model).execSelect()
                .forEachRemaining(
                        qs -> tmpActual[0] = qs.get("g").asNode().getLiteralLexicalForm());
        String actual = tmpActual[0];
        System.out.println(tmpActual[0]);
        String expected =
                "{\"type\":\"LineString\",\"coordinates\":[[11.120116,46.069743],[11.120095,46.069829]]}";
        GeometryFactory geometryFactory = new GeometryFactory();
        GeoJsonReader reader = new GeoJsonReader(geometryFactory);
        LineString expectedGeo = (LineString) reader.read(expected);
        LineString actualGeo = (LineString) reader.read(tmpActual[0]);
        Assert.assertEquals(true, expectedGeo.equalsExact(actualGeo, TOLERANCE));
    }

    public static String evalToLexicalForm(String exprStr) {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());
        String result = ExprUtils.eval(ExprUtils.parse(exprStr, pm)).asNode().getLiteralLexicalForm();
        return result;
    }

    @Test
    public void testLineMerge01() {
        String actual = evalToLexicalForm("geof:lineMerge('GEOMETRYCOLLECTION(LINESTRING(0 0, 5 5), LINESTRING(5 5, 10 10))'^^geo:wktLiteral)");
        Assert.assertEquals("LINESTRING(0 0, 5 5, 10 10)", actual);
    }

    @Test
    public void testLineMerge02() {
        String actual = evalToLexicalForm("geof:lineMerge('GEOMETRYCOLLECTION(LINESTRING(5 5, 10 10), LINESTRING(0 0, 5 5))'^^geo:wktLiteral)");
        Assert.assertEquals("LINESTRING(0 0, 5 5, 10 10)", actual);
    }

    @Test
    public void testUnion01() {
        String actual = evalToLexicalForm("geof:union('GEOMETRYCOLLECTION(POLYGON((-7 4.2,-7.1 4.2,-7.1 4.3, -7 4.2)),POINT(5 5),POINT(-2 3),LINESTRING(5 5, 10 10))'^^geo:wktLiteral)");
        Assert.assertEquals("GEOMETRYCOLLECTION(POINT(-2 3), LINESTRING(5 5, 10 10), POLYGON((-7 4.2, -7.1 4.2, -7.1 4.3, -7 4.2)))", actual);
    }

//	@Test
//	public void testNearestPoints() {
//		String queryStr = "PREFIX fn: <http://www.opengis.net/ont/geosparql#> SELECT ?g { BIND(fn:nearestPoints('POINT (0 0)', 'POINT (1 1)') AS ?g) }";
//		String[] tmpActual = {null};
//		QueryExecutionFactory.create(queryStr, ModelFactory.createDefaultModel()).execSelect().forEachRemaining(qs -> tmpActual[0] = qs.get("g").asNode().getLiteralLexicalForm());
//
//		String actual = tmpActual[0];
//		// TODO Compare the geometry objects
//		String expected = "LINESTRING (0 0, 1 1)";
//		Assert.assertEquals(expected, actual);
//	}
}
