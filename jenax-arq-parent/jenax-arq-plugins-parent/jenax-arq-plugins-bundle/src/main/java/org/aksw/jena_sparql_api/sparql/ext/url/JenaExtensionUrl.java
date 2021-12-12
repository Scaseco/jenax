package org.aksw.jena_sparql_api.sparql.ext.url;

import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionUrl {
    public static String ns = "http://jsa.aksw.org/fn/url/";

    // Should we distinguish between url and iri functions - e.g. resolution only makes sense for urls;
    public static String ns2 = "http://jsa.aksw.org/fn/iri/";

    public static void register() {
        FunctionRegistry.get().put(ns + "text", E_UrlText.class);
        FunctionRegistry.get().put(ns + "normalize", E_UrlNormalize.class);

        PropertyFunctionRegistry.get().put(ns + "text", new PropertyFunctionFactoryUrlText());

        JenaExtensionUtil.getDefaultFunctionBinder()
            .register(ns2 + "asGiven", NodeFactory.class, "createURI", String.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("url", ns);
        pm.setNsPrefix("iri", ns2);
    }
}
