package org.aksw.jena_sparql_api.sparql.ext.collection.base;

import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionCollection {
    public static final String NS = "http://jsa.aksw.org/fn/collection/";


    public static void register() {
        loadDefs(FunctionRegistry.get());
    }

    public static void loadDefs(FunctionRegistry registry) {
        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();

        binder.registerAll(SparqlLibCollectionFn.class);

       PropertyFunctionRegistry.get().put(NS + "unnest", PF_CollectionUnnest.class);
        // PropertyFunctionRegistry.get().put(NS + "explode", PF_ArrayExplode.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("collection", NS);
    }

}
