package org.aksw.jena_sparql_api.sparql.ext.nodemap;

import org.aksw.jenax.norse.NorseTerms;

public class NorseTermsNodeMap {
    public static final String Datatype = NorseTerms.NS + "nodeMap";

    /** Base namespace for all types of JSON-related functions (conventional functions, property functions, aggregators, etc) */
    public static final String NS = NorseTerms.NS + "nodeMap.";

    public static final String strictGet = NS + "strictGet";
}
