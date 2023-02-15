package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionJson {
    public static final String ns = "http://jsa.aksw.org/fn/json/";

    public static void register() {
//        AggregateRegistry.register(
//                ns + "collect",
//                AccumulatorFactories.wrap1(AggregatorsJena::aggGeometryCollection));

        FunctionRegistry fnReg = FunctionRegistry.get();
        fnReg.put(ns + "object", E_JsonObject.class);
        fnReg.put(ns + "array", E_JsonArray.class);
        fnReg.put(ns + "convert", E_JsonConvert.class);

//		fnReg.put(ns + "parse", E_JsonParse.class);
        fnReg.put(ns + "path", E_JsonPath.class);
        fnReg.put(ns + "entries", E_JsonEntries.class);
        fnReg.put(ns + "js", E_JsonNashorn.class);

        fnReg.put(ns + "split", E_JsonStrSplit.class);
        fnReg.put(ns + "reverse", E_JsonReverse.class);

        // TODO Move to a different namespace
        fnReg.put(ns + "binaryString", E_BinaryString.class);

        TypeMapper.getInstance().registerDatatype(new RDFDatatypeJson());

        PropertyFunctionRegistry pfnReg = PropertyFunctionRegistry.get();
        pfnReg.put(ns + "unnest", new PropertyFunctionFactoryJsonUnnest());
        pfnReg.put(ns + "explode", new PropertyFunctionFactoryJsonExplode());

        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
        binder.registerAll(SparqlFnLibJson.class);

    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("json", ns);

        // TODO Should be done elsewhere
        pm.setNsPrefix("math", ARQConstants.mathPrefix);
    }
}
