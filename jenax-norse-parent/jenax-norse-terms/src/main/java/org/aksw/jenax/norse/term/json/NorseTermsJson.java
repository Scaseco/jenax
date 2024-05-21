package org.aksw.jenax.norse.term.json;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsJson {
    /** The IRI for the JSON datatype */
    public static final String Datatype = NorseTerms.NS + "json";

    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "json.";

    public static final String get = NS + "get";
    public static final String getStrict = NS + "getStrict";
    public static final String path = NS + "path";
}
