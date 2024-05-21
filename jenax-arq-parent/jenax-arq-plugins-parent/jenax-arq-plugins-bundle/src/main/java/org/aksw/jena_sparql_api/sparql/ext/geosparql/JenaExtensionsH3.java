package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.norse.term.core.NorseTerms;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.sedona.common.Functions;
import org.apache.sedona.common.utils.H3Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;

public class JenaExtensionsH3 {
    private static final Logger logger = LoggerFactory.getLogger(JenaExtensionsH3.class);

    public static final String NS = NorseTerms.NS + "h3.";

    private static final H3Core h3 = getOrNull();

    public static H3Core getOrNull() {
        H3Core result;
        try {
            result = H3Core.newInstance();
        } catch (IOException e) {
            result = null;
            logger.warn("Failed to initialize uber's H3 spatial grid library. Functionality will not be available", e);
        }
        return result;
    }

    public static void init(FunctionBinder binder, PropertyFunctionRegistry ppfRegistry) {
        initFunctions(binder);
        initPropertyFunctions(ppfRegistry);
    }

    public static void initFunctions(FunctionBinder binder) {
        try {
            initCore(binder);
        } catch (Exception e) {
            logger.warn("Error while initializing H3 - some functionality will be unavailable", e);
        }
    }

    public static void initPropertyFunctions(PropertyFunctionRegistry ppfRegistry) {
        ppfRegistry.put(NS + "geomToCells", H3GeometryToCellIDsPF.class);
        ppfRegistry.put(NS + "cellToChildren", H3CellToChildrenPF.class);
        ppfRegistry.put(NS + "gridDisk", H3GridDiskPF.class);

        ppfRegistry.put(GeoSPARQL_URI.GEO_URI + "h3_geometryToCellIds", H3GeometryToCellIDsPF.class);
        ppfRegistry.put(GeoSPARQL_URI.GEO_URI + "h3_cellIdToChildren", H3CellToChildrenPF.class);
        ppfRegistry.put(GeoSPARQL_URI.GEO_URI + "h3_gridDisk", H3GridDiskPF.class);
    }

    // TODO Improve the binder API so that it can warn if methods can't be registered
    public static void initCore(FunctionBinder binder) throws NoSuchMethodException, SecurityException {

        // Returns long
        binder.register(NS + "latLngToCell",
                H3Core.class.getMethod("latLngToCell", Double.TYPE, Double.TYPE, Integer.TYPE), h3);

        binder.register(NS + "latLngToCellAddress",
                H3Core.class.getMethod("latLngToCellAddress", Double.TYPE, Double.TYPE, Integer.TYPE), h3);

        binder.registerAll(JenaExtensionsH3.class);

        // h3.latLngToCellAddress(lat, lng, res);
        //h3.cellToGeoBoundary(hexAddr);
    }

    @Iri(NS + "cellToPolygon")
    public static GeometryWrapper cellToPolygon(long addr) {
        List<LatLng> latlngs = h3.cellToBoundary(addr);
        List<Coordinate> coords = h3ToJts(latlngs);
        // Close the polygon by appending the first coordinate
        coords.add(coords.get(0));
        GeometryWrapper result = GeometryWrapperFactory.createPolygon(coords, Geo.WKT);
        return result;
    }

    /* Methods to bride H3-JTS */

    public static Coordinate h3ToJts(LatLng latLng) {
        return new Coordinate(latLng.lng, latLng.lat);
    }

    public static List<Coordinate> h3ToJts(List<LatLng> latLngs) {
        return latLngs.stream()
                .map(JenaExtensionsH3::h3ToJts)
                .collect(Collectors
                .toCollection(() -> new ArrayList<>(latLngs.size())));
    }

    @Iri(NS + "cells")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static NodeList h3CellIDs(GeometryWrapper geomWrapper, int level, boolean fullCover) {
        Long[] ids = Functions.h3CellIDs(geomWrapper.getParsingGeometry(), level, fullCover);
        NodeListImpl nodeList = new NodeListImpl(Arrays.stream(ids).map(NodeFactoryExtra::intToNode).collect(Collectors.toList()));
        return nodeList;
    }

    @Iri(NS + "lonLatToCell")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static long h3LongLatAsCellId(double lon, double lat, int resolution) {
        long cellId = H3Utils.h3.latLngToCell(lat, lon, resolution);
        return cellId;
    }

    @Iri(NS + "cellToGeom")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static GeometryWrapper h3CellIdToGeom(long cellId) {
        Geometry geom = H3ToGeometryAgg.h3ToGeom(new long[]{cellId});
        return GeometryWrapperFactory.createGeometry(geom, WKTDatatype.URI);
    }

    @Iri(NS + "cellToParent")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static long h3CellIdToParent(long cellId, int parentRes) {
        long parentId = H3Utils.h3.cellToParent(cellId, parentRes);
        return parentId;
    }

    @Iri(NS + "isValidCell")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static boolean h3IsValidCell(long cellId) {
        return H3Utils.h3.isValidCell(cellId);
    }

    @Iri(NS + "resolution")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static int h3CellResolution(long cellId) {
        return H3Utils.h3.getResolution(cellId);
    }

    @Iri(NS + "gridDistance")
    @IriNs(GeoSPARQL_URI.GEOF_URI)
    public static long h3GridDistance(long cellId1, long cellId2) {
        return H3Utils.h3.gridDistance(cellId1, cellId2);
    }
}
