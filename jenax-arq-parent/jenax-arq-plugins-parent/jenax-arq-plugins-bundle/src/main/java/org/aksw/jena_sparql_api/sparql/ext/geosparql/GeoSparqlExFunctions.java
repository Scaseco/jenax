package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.norse.term.geo.NorseTermsGeo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.FmtUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.PolygonHullSimplifier;
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

    @Iri(NorseTermsGeo.collect)
    public static GeometryWrapper collect(Node ... nodes) { //, @DefaultValue("false") boolean distinct, @DefaultValue("false") boolean unwrapSingle) {
        return collect(NodeListImpl.wrap(nodes));
    }

    @Iri(NorseTermsGeo.asCollection)
    public static GeometryWrapper collect(NodeList nodeCollection) {
        Aggregator<Binding, FunctionEnv, GeometryWrapper> agg =
            GeoSparqlExAggregators.aggGeometryWrapperCollection(new ExprVar(Vars.x), false, false);

        // XXX Performance-wise not ideal to allocate dummy binding objects here
        Optional<GeometryWrapper> r = agg.accumulateAll(nodeCollection.stream()
                .map(node -> BindingFactory.binding(Vars.x, node)), null);
        return r.orElse(null);
    }

    @Iri(NorseTermsGeo.unwrapSingle)
    public static GeometryWrapper collect(GeometryWrapper geom, @DefaultValue("false") boolean recursive) {
        Geometry init = geom.getParsingGeometry();
        Geometry current = init;
        do {
            if (!(current instanceof GeometryCollection)) {
                break;
            }

            GeometryCollection c = (GeometryCollection)current;
            if (c.getNumGeometries() != 1) {
                break;
            }

            current = c.getGeometryN(0);
        } while (recursive);

        GeometryWrapper result = current == init
                ? geom
                : GeometryWrapperUtils.createFromPrototype(geom, current);
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

    @IriNs(GeoSPARQL_URI.SPATIAL_FUNCTION_URI)
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


    @IriNs(GeoSPARQL_URI.SPATIAL_FUNCTION_URI)
    public static NodeList dbscan(NodeList arr, int geoIdx, double eps, int minPts) {
        return DbscanPf.dbscan(arr, geoIdx, eps, minPts);
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper makeLine(GeometryWrapper geomWrapper) {
        GeometryFactory f = CustomGeometryFactory.theInstance();

        Geometry inGeom = geomWrapper.getParsingGeometry();
        GeometryCollection c = inGeom instanceof GeometryCollection
            ? (GeometryCollection)inGeom
            : f.createGeometryCollection(new Geometry[] { inGeom });

        Coordinate[] coords = c.getCoordinates();

        Geometry outGeom = CustomGeometryFactory.theInstance().createLineString(coords);
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geomWrapper, outGeom);
        return result;
    }

//    public static void main(String[] args) {
//        GeometryWrapper start = GeometryWrapper.fromPoint(0, 0, SRS_URI.DEFAULT_WKT_CRS84);
//        System.out.println(project(start, 1000, 1 * Math.PI));
//    }

    /**
     * https://postgis.net/docs/ST_Project.html - azimuth
     * Source: https://stackoverflow.com/questions/44419722/calculate-coordinates-from-coordinates-distance-and-an-angle
     */
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper project(GeometryWrapper geomWrapper, double distanceInMeters, double azimuthInRadians) {
        double earthRadius = 6371e3;
        double srcLonDeg = lon(geomWrapper);
        double srcLatDeg = lat(geomWrapper);

        double srcLonRad = Math.toRadians(srcLonDeg);
        double srcLatRad = Math.toRadians(srcLatDeg);

        double angularDistance = distanceInMeters / earthRadius;

        double destLatRad = Math.asin(Math.sin(srcLatRad) * Math.cos(angularDistance) +
                Math.cos(srcLatRad) * Math.sin(angularDistance) * Math.cos(azimuthInRadians));

        double destLonRad = srcLonRad + Math.atan2(Math.sin(azimuthInRadians) * Math.sin(angularDistance) * Math.cos(srcLatRad),
                Math.cos(angularDistance) - Math.sin(srcLatRad) * Math.sin(destLatRad));

        // Normalize longitude to [-PI..+PI)
        destLonRad = (destLonRad + 3 * Math.PI) % (2.0 * Math.PI) - Math.PI;

        double destLonDeg = Math.toDegrees(destLonRad);
        double destLatDeg = Math.toDegrees(destLatRad);

        Geometry outGeom = CustomGeometryFactory.theInstance().createPoint(new Coordinate(destLonDeg, destLatDeg));
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geomWrapper, outGeom);
        return result;
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

    // GeoSPARQL 1.1
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double area(GeometryWrapper geom, Node areaUnitsURI) {

        if (!areaUnitsURI.isURI()) {
            throw new ExprEvalException("Not a IRI for area unit: " + FmtUtils.stringForNode(areaUnitsURI));
        }

        Geometry g = geom.getXYGeometry();

        // according to standard: "Must return zero for all geometry types other than Polygon. "
        if (!g.getGeometryType().equals("Polygon")) {
            return 0d;
        }

        double area = g.getArea();

        String unitsURI = geom.getUnitsOfMeasure().getUnitURI();

        double areaConverted = UnitsOfMeasure.conversion(area, unitsURI, areaUnitsURI.getURI());

        return areaConverted;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static double metricArea(GeometryWrapper geom) {
        return area(geom, NodeFactory.createURI(Unit_URI.METRE_URL));
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static boolean isValid(GeometryWrapper geom) {
        return geom.isValid();
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper makeValid(GeometryWrapper geomWrapper) {
        Geometry fixedGeometry = GeometryFixer.fix(geomWrapper.getParsingGeometry());
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geomWrapper, fixedGeometry);
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper hullByAreaDelta(GeometryWrapper geomWrapper, boolean isOuter, double areaDeltaRatio) {
        Geometry hull = PolygonHullSimplifier.hullByAreaDelta(geomWrapper.getParsingGeometry(), isOuter, areaDeltaRatio);
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geomWrapper, hull);
        return result;
    }

    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper hullByVertexNumberFraction(GeometryWrapper geomWrapper, boolean isOuter, double vertexNumFraction) {
        Geometry hull = PolygonHullSimplifier.hull(geomWrapper.getParsingGeometry(), isOuter, vertexNumFraction);
        GeometryWrapper result = GeometryWrapperUtils.createFromPrototype(geomWrapper, hull);
        return result;
    }
}
