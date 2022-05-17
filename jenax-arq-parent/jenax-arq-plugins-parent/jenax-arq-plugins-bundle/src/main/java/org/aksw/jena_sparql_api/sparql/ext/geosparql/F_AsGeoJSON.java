package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.vocabulary.RDF;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class F_AsGeoJSON
	extends FunctionBase1 {

	private GeoJsonWriter writer = new GeoJsonWriter();
	public F_AsGeoJSON() {
		writer.setForceCCW(true);
		// removed from GeoJSON 2016
		writer.setEncodeCRS(false);
	}

	@Override
	public NodeValue exec(NodeValue v) {
		if (!v.isLiteral()) {
			throw new ARQInternalErrorException("Not a literal: " + v) ;
		}
		try {
			GeometryWrapper gw = GeometryWrapper.extract(v);
			// GeoJSON 2016 removed support for other crs, need to transform to CRS 84
			GeometryWrapper convertedGeom = gw.transform(SRS_URI.DEFAULT_WKT_CRS84);

			String json = writer.write(convertedGeom.getParsingGeometry());

			Node node = NodeFactory.createLiteralByValue(json, RDF.dtRDFJSON);
			NodeValue result = NodeValue.makeNode(node);

			return result;
		} catch (Exception e){
			throw new ARQInternalErrorException("Failed to parse literal. Not a geometry?", e);
		}
	}
}
