package org.aksw.jena_sparql_api.sparql.ext.collection.array;

import org.aksw.jena_sparql_api.sparql.ext.collection.base.PF_CollectionExplode;
import org.aksw.jena_sparql_api.sparql.ext.collection.base.PF_CollectionUnnest;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.norse.term.core.NorseTerms;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionArray {
    public static final String NS = NorseTerms.NS + "array.";
    public static final String LEGACY_NS = "http://jsa.aksw.org/fn/array/";

    public static void register() {
        loadDefs(FunctionRegistry.get(), LEGACY_NS);
        loadDefs(FunctionRegistry.get(), NS);

        loadAggs(LEGACY_NS);
        loadAggs(NS);
    }

    public static void loadAggs(String prefix) {
        AggregateRegistry.register(
                prefix + "collect",
                SparqlLibArrayAgg.wrap1(SparqlLibArrayAgg::aggNodeList));

    }

    public static void loadDefs(FunctionRegistry registry, String prefix) {
        // Datatype is registered in the jenax-arq-datatype module!
        // TypeMapper.getInstance().registerDatatype(RDFDatatypeNodeList.get());

        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
//        FunctionGenerator generator = binder.getFunctionGenerator();
//
//        // Define two-way Geometry - GeometryWrapper coercions
//        generator.getConverterRegistry()
//            .register(Geometry.class, GeometryWrapper.class,
//                    geometry -> new GeometryWrapper(geometry, WKTDatatype.URI),
//                    GeometryWrapper::getParsingGeometry)
//            ;
//

        binder.registerAll(SparqlLibArrayFn.class);

        PropertyFunctionRegistry.get().put(prefix + "unnest", PF_CollectionUnnest.class);
        PropertyFunctionRegistry.get().put(prefix + "explode", PF_CollectionExplode.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("array", LEGACY_NS);
        pm.setNsPrefix("array", NS);
    }

}

