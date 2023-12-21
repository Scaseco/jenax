package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.norse.NorseTerms;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;

public class JenaBindingH3 {
    private static final Logger logger = LoggerFactory.getLogger(JenaBindingH3.class);

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

    public static void init(FunctionBinder binder) {
        try {
            initCore(binder);
        } catch (Exception e) {
            logger.warn("Error while initializing H3 - some functionality will be unavailable", e);
        }
    }

    // TODO Improve the binder API so that it can warn if methods can't be registered
    public static void initCore(FunctionBinder binder) throws NoSuchMethodException, SecurityException {

        // Returns long
        binder.register(NS + "latLngToCell",
                H3Core.class.getMethod("latLngToCell", Double.TYPE, Double.TYPE, Integer.TYPE), h3);

        binder.register(NS + "latLngToCellAddress",
                H3Core.class.getMethod("latLngToCellAddress", Double.TYPE, Double.TYPE, Integer.TYPE), h3);

        binder.registerAll(JenaBindingH3.class);

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
        return latLngs.stream().map(JenaBindingH3::h3ToJts).collect(Collectors.toCollection(() -> new ArrayList<>(latLngs.size())));
    }
}
