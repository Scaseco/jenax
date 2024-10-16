package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.norse.term.json.NorseTermsJson;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.vocabulary.XSD;

import com.google.gson.JsonElement;

public class JenaExtensionJson {
    public static final String LEGACY_NS = "http://jsa.aksw.org/fn/json/";

    public static void register() {
//        AggregateRegistry.register(
//                ns + "collect",
//                AccumulatorFactories.wrap1(AggregatorsJena::aggGeometryCollection));

        FunctionRegistry fnReg = FunctionRegistry.get();
        TypeMapper typeMapper = TypeMapper.getInstance();
        PropertyFunctionRegistry pfnReg = PropertyFunctionRegistry.get();
        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();

        // If there is more than one datatype for the JsonElement class then use the one with the given IRI
        binder.getFunctionGenerator().getTypeByClassOverrides().put(JsonElement.class, NorseTermsJson.Datatype);

        typeMapper.registerDatatype(new RDFDatatypeJson(NorseTermsJson.Datatype));
        registerFunctions(fnReg, NorseTermsJson.NS);
        registerPropertyFunctions(pfnReg, NorseTermsJson.NS);

        // TODO We yet need to register the legacy functions in the new namespace
        binder.registerAll(SparqlFnLibJson.class);

        // Registrations under legacy namespace(s)
        typeMapper.registerDatatype(new RDFDatatypeJson(XSD.getURI() + "json"));
        registerFunctions(fnReg, LEGACY_NS);
        registerPropertyFunctions(pfnReg, LEGACY_NS);
    }


    public static void registerPropertyFunctions(PropertyFunctionRegistry pfnReg, String ns) {
        pfnReg.put(ns + "unnest", new PropertyFunctionFactoryJsonUnnest());
        pfnReg.put(ns + "explode", new PropertyFunctionFactoryJsonExplode());
    }

    public static void registerFunctions(FunctionRegistry fnReg, String ns) {
        fnReg.put(ns + "object", E_JsonObject.class);
        fnReg.put(ns + "array", E_JsonArray.class);
        fnReg.put(ns + "convert", E_JsonConvert.class);

//		fnReg.put(ns + "parse", E_JsonParse.class);
        fnReg.put(ns + "path", E_JsonPath.class);
        fnReg.put(ns + "get", E_JsonGet.class);
        fnReg.put(ns + "getStrict", E_JsonGetStrict.class);
        fnReg.put(ns + "entries", E_JsonEntries.class);
        fnReg.put(ns + "js", E_JsonNashorn.class);
        fnReg.put(ns + "je", E_JsonNashorn.class);

        fnReg.put(ns + "split", E_JsonStrSplit.class);
        fnReg.put(ns + "reverse", E_JsonReverse.class);

        // TODO Move to a different namespace
        fnReg.put(ns + "binaryString", E_BinaryString.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("json", LEGACY_NS);

        // TODO Should be done elsewhere
        pm.setNsPrefix("math", ARQConstants.mathPrefix);
    }
}
