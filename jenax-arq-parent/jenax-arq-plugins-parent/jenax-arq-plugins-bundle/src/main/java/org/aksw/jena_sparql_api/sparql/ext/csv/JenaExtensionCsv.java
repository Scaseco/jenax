package org.aksw.jena_sparql_api.sparql.ext.csv;

import org.aksw.jenax.norse.term.csv.NorseTermsCsv;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionCsv {
    public static final String ns = "http://jsa.aksw.org/fn/csv/";

    public static void register() {
        FunctionRegistry.get().put(NorseTermsCsv.parse, E_CsvParse.class);
        FunctionRegistry.get().put(ns + "parse", E_CsvParse.class);

        PropertyFunctionRegistry.get().put(NorseTermsCsv.parse, new PropertyFunctionFactoryCsvParse());
        PropertyFunctionRegistry.get().put(ns + "parse", new PropertyFunctionFactoryCsvParse());
    }

    public static void addPrefixes(PrefixMapping pm) {
        //pm.setNsPrefix("csv", ns);
        pm.setNsPrefix("csv", NorseTermsCsv.NS);
    }
}
