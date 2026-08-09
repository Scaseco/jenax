package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.norse.term.geo.NorseTermsGeo;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeoSparqlExWktFunctions {
    @Iri(NorseTermsGeo.NS + "wkt.fixStructure")
    public static String wktFixStructure(String wktStr) {
        WKTReader reader = new WKTReader();
        reader.setFixStructure(true);

        Geometry geom;
        try {
            geom = reader.read(wktStr);
        } catch (ParseException e) {
            throw new ExprEvalException(e);
        }
        // Note that the geom may still be invalid. Use geof:isValid to verify.
        WKTWriter writer = new WKTWriter();
        String fixedStr = writer.write(geom);

        // Node result = NodeFactory.createLiteralDT(fixedStr, WKTDatatype.INSTANCE);
        return fixedStr;
    }
}
