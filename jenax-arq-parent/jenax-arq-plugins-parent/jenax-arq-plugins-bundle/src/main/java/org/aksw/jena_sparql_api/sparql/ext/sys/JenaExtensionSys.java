package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_CompareResultSet;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_NextLong;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.FN_Benchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.FN_BenchmarkOld;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.FN_SparqlQueryRewrite_ToService;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryBenchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryExecSelect;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.norse.NorseTerms;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionSys {
    public static String ns = "http://jsa.aksw.org/fn/sys/";

    public static void register() {

        PropertyFunctionRegistry pfRegistry = PropertyFunctionRegistry.get();

        pfRegistry.put(ns + "benchmark", new PropertyFunctionFactoryBenchmark());
        pfRegistry.put(ns + "execSelect", new PropertyFunctionFactoryExecSelect());
        pfRegistry.put(ns + "listFunctions", new PropertyFunctionFactoryListFunctions());
        pfRegistry.put(ns + "listPropertyFunctions", new PropertyFunctionFactoryListPropertyFunctions());
        pfRegistry.put(ns + "listAggregateFunctions", new PropertyFunctionFactoryListAggregateFunctions());

        FunctionRegistry registry = FunctionRegistry.get();

        registry.put(ns + "benchmarkOld", FN_BenchmarkOld.class);
        registry.put(NorseTerms.NS + "sys." + "benchmark", FN_Benchmark.class);
        registry.put(NorseTerms.NS + "sys." + "err.print", FN_PrintErr.class);
        registry.put(ns + "nextLong", E_NextLong.class);
        registry.put(ns + "rscmp", E_CompareResultSet.class);

        registry.put(NorseLambdaTerms.fnOf, FN_LambdaOf.class);
        registry.put(NorseLambdaTerms.fnCall, FN_LambdaCall.class);
        registry.put(NorseLambdaTerms.mapComputeIfAbsent, FN_MapComputeIfAbsent.class);

        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
        binder.register(ns + "getenv", System.class, "getenv", String.class);
        binder.register(ns + "getProperty", System.class, "getProperty", String.class);

        registry.put(NorseTerms.NS + "sparql.rewrite."+ "toService", FN_SparqlQueryRewrite_ToService.class);

    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("sys", ns);
    }
}
