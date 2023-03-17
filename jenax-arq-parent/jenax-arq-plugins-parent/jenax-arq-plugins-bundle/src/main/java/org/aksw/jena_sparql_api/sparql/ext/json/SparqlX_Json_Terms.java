package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jena_sparql_api.sparql.ext.init.SparqlX;

public class SparqlX_Json_Terms {
	/** The IRI for the JSON datatype */
	public static final String Datatype = SparqlX.NS + "json"; 
	
	/** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = SparqlX.NS + "json.";

    public static final String get = NS + "get";
    public static final String path = NS + "path";
}
