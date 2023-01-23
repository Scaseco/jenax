package org.aksw.jena_sparql_api.sparql.ext.number;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionNumber {
    public static String ns = "http://jsa.aksw.org/fn/number/";

    public static void register() {

        PropertyFunctionRegistry pfRegistry = PropertyFunctionRegistry.get();

        pfRegistry.put(ns + "range", PF_Range.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("number", ns);
    }
}
