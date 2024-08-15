package org.aksw.jenax.norse.term.rdf;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsNodeMap {
    public static final String Datatype = NorseTerms.NS + "nodeMap";

    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "nodeMap.";

    /** Gets a value from the node map. Fails the query execution if the accessed key is not present in the map. */
    public static final String getStrict = NS + "getStrict";
}
