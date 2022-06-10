package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

public class GeoSparqlExFunctions {

    /** expands geometry collection to an array */
    public static Stream<Geometry> expandCollection(Geometry geom) {
        Stream<Geometry> result;
        if (geom instanceof GeometryCollection) {
            GeometryCollection c = ((GeometryCollection)geom);
            result = IntStream.range(0, c.getNumGeometries())
                .mapToObj(i -> c.getGeometryN(i));
        } else {
            result = Stream.of(geom);
        }

        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper simplifyDp(
            GeometryWrapper geom,
            @DefaultValue("0") double tolerance,
            @DefaultValue("true") boolean ensureValid) {
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(geom.getParsingGeometry());
        simplifier.setDistanceTolerance(tolerance);
        simplifier.setEnsureValid(ensureValid);
        Geometry tmp = simplifier.getResultGeometry();

        return GeometryWrapperUtils.createFromPrototype(geom, tmp);
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper union(GeometryWrapper geom) {
        // List<Geometry> list = expandCollection(geom.getParsingGeometry()).collect(Collectors.toList());
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom,
                UnaryUnionOp.union(geom.getParsingGeometry()));
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper lineMerge(GeometryWrapper geom) {
        LineMerger merger = new LineMerger();
        expandCollection(geom.getParsingGeometry()).forEach(merger::add);
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom,
                UnaryUnionOp.union(merger.getMergedLineStrings()));
        return result;
    }



//	@IriNs(GeoSPARQL_URI.GEOF_URI)
//	public static Geometry centroid(
//			Geometry geom) {
//		Geometry result = geom.getCentroid();
//		return result;
//	}

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper centroid(GeometryWrapper geom) {
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom,
                geom.getParsingGeometry().getCentroid());
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double x(Geometry geom) {
        if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
        return ((Point)geom).getX();
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double y(Geometry geom) {
        if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
        return ((Point)geom).getY();
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double lon(GeometryWrapper geom) {
        return x(GeometryWrapperUtils.toWgs84(geom).getParsingGeometry());
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double lat(GeometryWrapper geom) {
        return y(GeometryWrapperUtils.toWgs84(geom).getParsingGeometry());
    }

//	@IriNs(GeoSPARQL_URI.GEOF_URI)
//	public static double lat(Geometry geom) {
//		if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
//		return ((Point)geom).getX();
//	}
//
//	@IriNs(GeoSPARQL_URI.GEOF_URI)
//	public static double lon(Geometry geom) {
//		if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
//		return ((Point)geom).getY();
//	}


}
