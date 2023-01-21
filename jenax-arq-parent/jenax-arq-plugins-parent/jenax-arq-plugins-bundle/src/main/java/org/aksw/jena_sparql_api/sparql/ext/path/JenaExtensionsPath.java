package org.aksw.jena_sparql_api.sparql.ext.path;

import org.aksw.jena_sparql_api.sparql_path2.PropertyFunctionFactoryKShortestPaths;
import org.aksw.jena_sparql_api.sparql_path2.PropertyFunctionKShortestPaths;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionsPath {
    public static String ns = "http://jsa.aksw.org/fn/path/";

    public static void register() {
        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths(ss -> null));
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("path", ns);
    }
}
