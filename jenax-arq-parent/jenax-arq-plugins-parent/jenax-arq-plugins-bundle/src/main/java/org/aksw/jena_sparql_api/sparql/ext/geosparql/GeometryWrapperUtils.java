package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

public class GeometryWrapperUtils {

	/** Create a geometry wrapper from a jts geometry by using another geometry wrapper as the source of the srs and datatype */
	public static GeometryWrapper createFromPrototype(GeometryWrapper prototype, Geometry geom) {
		GeometryWrapper result = new GeometryWrapper(geom, prototype.getSrsURI(), prototype.getGeometryDatatypeURI());
		return result;
	}

	public static Geometry extractParsingGeometryOrNull(NodeValue nv) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(nv);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}
	
	public static GeometryWrapper extractGeometryWrapperOrNull(NodeValue nv) {
		GeometryWrapper result = null;
		try {
			result = GeometryWrapper.extract(nv);
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}

	public static GeometryWrapper extractGeometryWrapperOrNull(Node node) {
		GeometryWrapper result = null;
		try {
			result = GeometryWrapper.extract(node);
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}
	

	public static GeometryWrapper toWgs84(GeometryWrapper gw) {
		GeometryWrapper result;
		try {
			result = gw.convertSRS(SRS_URI.DEFAULT_WKT_CRS84);
		} catch (MismatchedDimensionException | FactoryException | TransformException e) {
			throw new ExprEvalException("Failed to convert geometry", e);
		}

		return result;
	}

	/** Return a geometry with x=lon y=lat*/
	public static Geometry getWgs84Geometry(GeometryWrapper gw) {
		return toWgs84(gw).getParsingGeometry();
	}

	public static Geometry extractWgs84GeometryOrNull(Node node) {
		
		Geometry result = Optional.ofNullable(extractGeometryWrapperOrNull(node))
				.map(GeometryWrapperUtils::getWgs84Geometry).orElse(null);		
		return result;
	}

	/** Discards values of nodes that are not geometries */
	public static List<Geometry> nodesToGeoms(Collection<Node> nodes) {
		List<Geometry> result = nodes.stream()
			.map(GeometryWrapperUtils::extractWgs84GeometryOrNull)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		return result;
	}

}
