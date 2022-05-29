package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

/**
 * Function that creates Voronoi Diagrams from collections of points.
 * The diagram is returned as a GeometryCollection of Polygons representing the faces of the Voronoi diagram.
 *
 * @see org.locationtech.jts.triangulate.VoronoiDiagramBuilder
 */
public class F_ST_VoronoiPolygons
        extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v, NodeValue toleranceV) {
        if (v != null) {
            GeometryWrapper geom = GeometryWrapper.extract(v);
            VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
            builder.setSites(geom.getXYGeometry());
            if (toleranceV != null && toleranceV.isDouble()) {
                double tolerance = toleranceV.getDouble();
                builder.setTolerance(tolerance);
            }
            Geometry voronoi = builder.getDiagram(new GeometryFactory());
            GeometryWrapper voronoiWrapper = GeometryWrapperFactory.createGeometry(voronoi, geom.getSrsURI(), geom.getGeometryDatatypeURI());
            return voronoiWrapper.asNodeValue();
        } else {
            throw new ExprEvalException("VoronoiPolygons requires geometry argument");
        }

    }
}
