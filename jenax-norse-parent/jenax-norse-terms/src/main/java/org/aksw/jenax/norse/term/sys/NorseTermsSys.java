package org.aksw.jenax.norse.term.sys;

import org.aksw.jenax.norse.term.core.NorseTerms;

public class NorseTermsSys {
    public static final String NS = NorseTerms.NS + "sys.";
    /** A query-execution scoped map of maps. Can be used to cache computed data during query execution as well as to pass data. */
    public static final String tableComputeIfAbsent = NorseTerms.NS + "table.computeIfAbsent";

    /** Function name to return the name of the thread that executes that function. */
    public static final String threadName = NS + "thread.name";

    /** A query-execution scoped map. Can be used to pass data into a query. */
    public static final String mapGet = NS + "map.get";
    public static final String mapGetStrict = NS + "map.getStrict";
}
