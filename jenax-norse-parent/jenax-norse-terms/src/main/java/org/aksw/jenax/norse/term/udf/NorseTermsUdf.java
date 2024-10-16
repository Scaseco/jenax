package org.aksw.jenax.norse.term.udf;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsUdf {
    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "udf.";

    public static final String UserDefinedFunction = NS + "UserDefinedFunction";
    public static final String profile = NS + "profile";

    // UdfDefinition
    public static final String expr = NS + "expr";
    public static final String params = NS + "params";
    public static final String inverse = NS + "inverse";

    public static final String simpleDefinition = NS + "simpleDefinition";
    public static final String definition = NS + "definition";

    public static final String aliasFor = NS + "aliasFor";
    public static final String mapsToPropertyFunction = NS + "mapsToPropertyFunction";

    // InverseDefinition
    public static final String ofParam = NS + "ofParam";
    public static final String ofFunction = NS + "ofFunction";
}
