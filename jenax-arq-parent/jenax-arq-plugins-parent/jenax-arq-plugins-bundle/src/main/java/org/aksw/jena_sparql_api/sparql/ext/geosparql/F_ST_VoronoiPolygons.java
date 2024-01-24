package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.function.FunctionBase3;
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
        extends FunctionBase3 {

    @Override
    public NodeValue exec(NodeValue v, NodeValue toleranceV, NodeValue envelope) {
        if (v != null) {
            GeometryWrapper geom = GeometryWrapper.extract(v);
            VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
            builder.setSites(geom.getXYGeometry());

            if (toleranceV != null && toleranceV.isDouble()) {
                double tolerance = toleranceV.getDouble();
                builder.setTolerance(tolerance);
            }

            GeometryWrapper env = null;
            if (envelope != null) {
                env = GeometryWrapper.extract(envelope);
                builder.setClipEnvelope(env.getEnvelope());
            }

            Geometry result = builder.getDiagram(CustomGeometryFactory.theInstance());
//            if (env != null) {
//                result = env.getParsingGeometry().intersection(result);
//            }

            GeometryWrapper voronoiWrapper = GeometryWrapperUtils.createFromPrototype(geom, result);
            return voronoiWrapper.asNodeValue();
        } else {
            throw new ExprEvalException("VoronoiPolygons requires geometry argument");
        }

    }
}
