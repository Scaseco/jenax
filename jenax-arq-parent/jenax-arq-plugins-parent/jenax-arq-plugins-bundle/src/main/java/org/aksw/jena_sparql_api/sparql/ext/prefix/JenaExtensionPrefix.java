package org.aksw.jena_sparql_api.sparql.ext.prefix;

import java.util.Set;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionPrefix {
    public static String ns = "http://jsa.aksw.org/fn/prefix/";

    // Tarql compatibility
    public static String tarqlNs = "http://tarql.github.io/tarql#";

    public static final String PREFIX_GET = ns + "get";
    public static final String PREFIX_EXPAND = ns + "expand";
    public static final String PREFIX_ABBREV = ns + "abbrev";

    public static final String TARQL_EXPAND_PREFIX = tarqlNs + "expandPrefix";
    public static final String TARQL_EXPAND_PREFIXED_NAME = tarqlNs + "expandPrefixedName";

    public static final Set<String> PREFIX_FUNCTIONS = Set.of(
        PREFIX_GET, PREFIX_EXPAND, PREFIX_ABBREV,
        TARQL_EXPAND_PREFIX, TARQL_EXPAND_PREFIXED_NAME);

    public static void register() {
        FunctionRegistry registry = FunctionRegistry.get();
        registry.put(ns + "get", E_PrefixGet.class);
        registry.put(ns + "expand", E_PrefixExpand.class);
        registry.put(ns + "abbrev", E_PrefixAbbrev.class);

        // Tarql compatibility
        putIfAbsent(registry, TARQL_EXPAND_PREFIX, E_PrefixGet.class);
        putIfAbsent(registry, TARQL_EXPAND_PREFIXED_NAME, E_PrefixExpand.class);
    }

    public static void putIfAbsent(FunctionRegistry registry, String iri, Class<?> cls) {
        if (registry.get(iri) == null) {
            registry.put(iri, cls);
        }
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("prefix", ns);
    }
}
