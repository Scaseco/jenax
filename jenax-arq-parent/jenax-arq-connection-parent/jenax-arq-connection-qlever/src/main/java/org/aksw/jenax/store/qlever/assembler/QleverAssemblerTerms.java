package org.aksw.jenax.store.qlever.assembler;

public class QleverAssemblerTerms {
    public static final String NS = "http://jena.apache.org/qlever#"; // DatasetAssemblerVocab.NS;

    public static final String Dataset = NS + "Dataset";

    public static final String location = NS + "location";
    public static final String indexName = NS + "indexName";
    public static final String port = NS + "port";
    public static final String accessToken = NS + "accessToken";

    public static final String numSimultaneousQueries = NS + "numSimultaneousQueries"; // Integer
    public static final String memoryMaxSize = NS + "memoryMaxSize";
    public static final String cacheMaxSize = NS + "cacheMaxSize";
    public static final String cacheMaxSizeSingleEntry = NS + "cacheMaxSizeSingleEntry";
    public static final String lazyResultMaxCacheSize = NS + "lazyResultMaxCacheSize";
    public static final String cacheMaxNumEntries = NS + "cacheMaxNumEntries"; // Long
    public static final String noPatterns = NS + "noPatterns"; // Boolean
    public static final String noPatternTrick = NS + "noPatternTrick"; // Boolean

    public static final String text = NS + "text";  // Boolean
    public static final String onlyPsoAndPosPermutations = NS + "onlyPsoAndPosPermutations"; // Boolean
    public static final String defaultQueryTimeout = NS + "defaultQueryTimeout";
    public static final String serviceMaxValueRows = NS + "serviceMaxValueRows"; // Long
    public static final String throwOnUnboundVariables = NS + "throwOnUnboundVariables"; // Boolean
}
