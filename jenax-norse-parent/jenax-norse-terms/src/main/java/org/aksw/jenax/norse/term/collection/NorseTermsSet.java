package org.aksw.jenax.norse.term.collection;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsSet {
    public static final String NS = NorseTerms.NS + "set.";

    public static final String datatype = NorseTerms.NS + "set";

    /** Perhaps rename to aggCollect */
    public static final String collect = NS + "collect";
    public static final String unnest = NS + "unnest";

    /** Merge NodeCollections (sets and arrays) into a single set.
     *  Arguments must be NodeCollections.
     */
    public static final String aggUnion = NS + "aggUnion";
}
