package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.math.DoubleMath;


public class TestSparqlExtGeoSparql {
	
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
		
		double lon = GeoFunctionsJena.lon(gw);
		Assert.assertTrue(DoubleMath.fuzzyEquals(89, lon, TOLERANCE));
	
		double lat = GeoFunctionsJena.lat(gw);
		Assert.assertTrue(DoubleMath.fuzzyEquals(179, lat, TOLERANCE));
	}

	@Test
	public void testCentroidViaExpr() {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());


		// rectangle with width 4 and height 2 - centroid expected at (2, 1)
		GeometryWrapper gw = GeometryWrapper.extract(
				ExprUtils.eval(ExprUtils.parse("geof:centroid('POLYGON((0 0, 4 0, 4 2, 0 2, 0 0))'^^geo:wktLiteral)", pm)));
		Assert.assertTrue(DoubleMath.fuzzyEquals(2, GeoFunctionsJena.x(gw.getParsingGeometry()), TOLERANCE));
		Assert.assertTrue(DoubleMath.fuzzyEquals(1, GeoFunctionsJena.y(gw.getParsingGeometry()), TOLERANCE));
		
//		System.out.println(gw);
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
