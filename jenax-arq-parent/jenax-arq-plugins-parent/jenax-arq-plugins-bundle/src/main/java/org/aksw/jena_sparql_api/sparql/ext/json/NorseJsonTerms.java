package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jenax.norse.NorseTerms;

public class NorseJsonTerms {
	/** The IRI for the JSON datatype */
	public static final String Datatype = NorseTerms.NS + "json"; 
	
	/** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "json.";

    public static final String get = NS + "get";
    public static final String path = NS + "path";
}
