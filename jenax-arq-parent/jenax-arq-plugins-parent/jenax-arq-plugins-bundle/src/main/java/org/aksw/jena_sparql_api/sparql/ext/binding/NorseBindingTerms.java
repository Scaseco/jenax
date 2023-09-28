package org.aksw.jena_sparql_api.sparql.ext.binding;

import org.aksw.jenax.norse.NorseTerms;

public class NorseBindingTerms {
    public static final String Datatype = NorseTerms.NS + "binding";

    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "binding.";

    public static final String get = NS + "get";
}
