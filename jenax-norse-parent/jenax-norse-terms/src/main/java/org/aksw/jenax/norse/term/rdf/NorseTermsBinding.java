package org.aksw.jenax.norse.term.rdf;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsBinding {
    public static final String Datatype = NorseTerms.NS + "binding";

    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "binding.";

    public static final String get = NS + "get";
}
