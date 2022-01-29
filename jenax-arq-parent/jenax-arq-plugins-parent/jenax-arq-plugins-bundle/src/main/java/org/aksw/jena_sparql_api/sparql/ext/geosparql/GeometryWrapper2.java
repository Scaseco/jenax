package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Geometry;

public class GeometryWrapper2 {

	public static Geometry extractGeometryOrNull(NodeValue nv) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(nv);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}
	
	public static Geometry extractGeometryOrNull(Node node) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(node);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}


}
