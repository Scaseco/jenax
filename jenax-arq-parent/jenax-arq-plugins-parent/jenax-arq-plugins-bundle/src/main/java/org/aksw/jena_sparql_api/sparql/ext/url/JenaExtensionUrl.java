package org.aksw.jena_sparql_api.sparql.ext.url;

import org.aksw.jena_sparql_api.sparql.ext.init.NorseTerms;
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

        FunctionRegistry.get().put(ns + "fetch", E_UrlFetch.class);
        FunctionRegistry.get().put(ns + "fetchSpec", E_UrlFetchSpec.class);

        PropertyFunctionRegistry.get().put(ns + "text", new PropertyFunctionFactoryUrlText());
        PropertyFunctionRegistry.get().put(ns + "textLines", new PropertyFunctionFactoryUrlTextAsLines());

        FunctionRegistry.get().put(ns2 + "asGiven", E_IriAsGiven.class);
        FunctionRegistry.get().put(NorseTerms.NS + "bnode.asGiven", F_BNodeAsGiven.class);

        // JenaExtensionUtil.getDefaultFunctionBinder()
         //   .register(ns2 + "asGiven", NodeFactory.class, "createURI", String.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("url", ns);
        pm.setNsPrefix("iri", ns2);
    }
}
