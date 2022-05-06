package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.vocabulary.RDF;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class F_AsGeoJSON
	extends FunctionBase1 {

	private GeoJsonWriter writer = new GeoJsonWriter();

	@Override
	public NodeValue exec(NodeValue v) {
		if (!v.isLiteral()) {
			throw new ARQInternalErrorException("Not a literal: " + v) ;
		}
		try {
			GeometryWrapper gw = GeometryWrapper.extract(v);
			Geometry geom = gw.getParsingGeometry();

			String json = writer.write(geom);

			Node node = NodeFactory.createLiteralByValue(json, RDF.dtRDFJSON);
			NodeValue result = NodeValue.makeNode(node);

			return result;
		} catch (Exception e){
			throw new ARQInternalErrorException("Failed to parse literal. Not a geometry?", e);
		}
	}
}
