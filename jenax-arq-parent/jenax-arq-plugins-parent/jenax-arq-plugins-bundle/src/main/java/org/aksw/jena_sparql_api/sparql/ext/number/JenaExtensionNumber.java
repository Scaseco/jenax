package org.aksw.jena_sparql_api.sparql.ext.number;

import org.aksw.jenax.norse.NorseTerms;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionNumber {
    public static String NS = NorseTerms.NS + "number.";
    public static String LEGACY_NS = "http://jsa.aksw.org/fn/number/";

    public static void register() {

        PropertyFunctionRegistry pfRegistry = PropertyFunctionRegistry.get();

        pfRegistry.put(NS + "range", PF_Range.class);
        pfRegistry.put(LEGACY_NS + "range", PF_Range.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("norse", NorseTerms.NS); // XXX Consolidate; Norse should be added once in one place
        pm.setNsPrefix("number", LEGACY_NS);
    }
}
