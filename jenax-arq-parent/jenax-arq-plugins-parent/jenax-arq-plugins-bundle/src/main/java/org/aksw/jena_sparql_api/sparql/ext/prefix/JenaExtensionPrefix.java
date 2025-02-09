package org.aksw.jena_sparql_api.sparql.ext.prefix;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionPrefix {
    public static String ns = "http://jsa.aksw.org/fn/prefix/";

    // Tarql compatibility
    public static String tarqlNs = "http://tarql.github.io/tarql#";

    public static void register() {
        FunctionRegistry registry = FunctionRegistry.get();
        registry.put(ns + "get", E_PrefixGet.class);
        registry.put(ns + "expand", E_PrefixExpand.class);
        registry.put(ns + "abbrev", E_PrefixAbbrev.class);

        // Tarql compatibility
        String iri;
        if (registry.get((iri = tarqlNs + "expandPrefix")) == null) {
            registry.put(iri, E_PrefixGet.class);
        }

        if (registry.get((iri = tarqlNs + "expandPrefixedName")) == null) {
            registry.put(iri, E_PrefixExpand.class);
        }
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("prefix", ns);
    }
}
