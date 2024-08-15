package org.aksw.jenax.norse.term.geo;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsGeo {
    public static final String NS = NorseTerms.NS + "geo.";

    public static final String aggCollect = NS + "aggCollect"; // norse:geo.agg.collect?

    public static final String collect = NS + "collect"; // norse:geo.fn.collect?
    public static final String asCollection = NS + "asCollection"; // Convert a node collection to a geometry collection
    public static final String unwrapSingle = NS + "unwrapSingle"; // If the argument is a geometry collection with a single element then return that element
}
