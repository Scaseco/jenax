package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.VWSimplifier;

public class GeoSparqlExFunctions {

    /** expands geometry collection to an array */
    public static Stream<Geometry> expandCollection(Geometry geom) {
        Stream<Geometry> result;
        if (geom instanceof GeometryCollection) {
            GeometryCollection c = ((GeometryCollection)geom);
            result = IntStream.range(0, c.getNumGeometries())
                .mapToObj(c::getGeometryN);
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
    public static GeometryWrapper simplifyVW(
            GeometryWrapper geom,
            @DefaultValue("0") double tolerance,
            @DefaultValue("true") boolean ensureValid) {
        VWSimplifier simplifier = new VWSimplifier(geom.getParsingGeometry());
        simplifier.setDistanceTolerance(tolerance);
        simplifier.setEnsureValid(ensureValid);
        Geometry tmp = simplifier.getResultGeometry();

        return GeometryWrapperUtils.createFromPrototype(geom, tmp);
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper union(GeometryWrapper geom) {
        // List<Geometry> list = expandCollection(geom.getParsingGeometry()).collect(Collectors.toList());
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom,
                OverlayNGRobust.union(geom.getParsingGeometry()));
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper intersection(GeometryWrapper geom1, GeometryWrapper geom2) {
        Geometry intersection = geom1.getParsingGeometry().intersection(geom2.getParsingGeometry());
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom1, intersection);
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper difference(GeometryWrapper geom1, GeometryWrapper geom2) {
        Geometry difference = geom1.getParsingGeometry().difference(geom2.getParsingGeometry());
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom1, difference);
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper lineMerge(GeometryWrapper geom) {
        LineMerger merger = new LineMerger();
        expandCollection(geom.getParsingGeometry()).forEach(merger::add);
        @SuppressWarnings("unchecked")
        Collection<Geometry> tmp = (Collection<Geometry>)merger.getMergedLineStrings();
        if (tmp.isEmpty()) {
            throw new ExprEvalException("No line strings have been input of geof:lineMerge function. Can't make union of empty line strings after merge step.");
        }
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geom,
                OverlayNGRobust.union(tmp));
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

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double length(GeometryWrapper geom) {
        Geometry g = geom.getParsingGeometry();
        if (g instanceof LineString || g instanceof MultiLineString) {
            return g.getLength();
        } else {
            return 0;
        }
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double perimeter(GeometryWrapper geom) {
        Geometry g = geom.getParsingGeometry();
        if (g instanceof Polygon || g instanceof MultiPolygon) {
            return g.getLength();
        } else {
            return 0;
        }
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double area(GeometryWrapper geom) {
        Geometry g = geom.getParsingGeometry();
        return g.getArea();
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
