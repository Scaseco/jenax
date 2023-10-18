package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class WGS84 {
    public static final String NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final Property lat = ResourceFactory.createProperty(NS + "lat");
    public static final Property xlong = ResourceFactory.createProperty(NS + "long");
}
