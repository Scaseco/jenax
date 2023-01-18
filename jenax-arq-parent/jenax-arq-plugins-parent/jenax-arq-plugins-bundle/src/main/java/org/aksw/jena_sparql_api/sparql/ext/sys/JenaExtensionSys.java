package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_Benchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_CompareResultSet;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_NextLong;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryBenchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryExecSelect;
import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
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

        registry.put(ns + "benchmark", E_Benchmark.class);
        registry.put(ns + "nextLong", E_NextLong.class);
        registry.put(ns + "rscmp", E_CompareResultSet.class);


        FunctionBinder binder = JenaExtensionUtil.getDefaultFunctionBinder();
        binder.register(ns + "getenv", System.class, "getenv", String.class);
        binder.register(ns + "getProperty", System.class, "getProperty", String.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("sys", ns);
    }
}
