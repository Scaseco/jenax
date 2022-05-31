package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class RDFDatatypeGeoJSON extends GeometryDatatype {

    /**
     * The default GeoJSON type URI.
     */
    public static final String URI = GeoSPARQL_URI.GEO_URI + "geoJSON";

    /**
     * A static instance of WKTDatatype.
     */
    public static final RDFDatatypeGeoJSON INSTANCE = new RDFDatatypeGeoJSON();

    /**
     * private constructor - single global instance.
     */
    private RDFDatatypeGeoJSON() {
        super(URI);
    }

    @Override
    public GeometryWrapper read(String geometryLiteral) {
        try {
            GeoJsonReader reader = new GeoJsonReader();
            Geometry geom = reader.read(geometryLiteral);
            GeometryWrapper wrapper = GeometryWrapperFactory.createGeometry(geom, "<http://www.opengis.net/def/crs/EPSG/0/" + geom.getSRID() + ">", RDFDatatypeGeoJSON.URI);
            return wrapper;
        } catch (ParseException e) {
            throw new DatatypeFormatException("Not a GeoJSON literal: " + geometryLiteral);
        }
    }

    @Override
    public String unparse(Object geometry) {
        if (geometry instanceof GeometryWrapper) {
            GeometryWrapper gw = (GeometryWrapper) geometry;
            GeoJsonWriter writer = new GeoJsonWriter();
            writer.setForceCCW(true);
            // removed from GeoJSON 2016
            writer.setEncodeCRS(false);
            try {
                // GeoJSON 2016 removed support for other crs, need to transform to CRS 84
                GeometryWrapper convertedGeom = gw.transform(SRS_URI.DEFAULT_WKT_CRS84);

                String json = writer.write(convertedGeom.getParsingGeometry());

                return json;
            } catch (Exception e) {
                throw new ARQInternalErrorException("Failed to write GeoJSON literal: " + geometry);
            }
        } else {
            throw new DatatypeFormatException("Object to unparse GeoJSON literal is not a GeometryWrapper");
        }
    }
}
