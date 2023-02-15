package org.aksw.jena_sparql_api.sparql.ext.collection.set;

import org.aksw.jena_sparql_api.sparql.ext.collection.base.PF_CollectionUnnest;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionSet {
    public static final String NS = "http://jsa.aksw.org/fn/set/";


    public static void register() {
        loadDefs(FunctionRegistry.get());

        AggregateRegistry.register(
                NS + "collect",
                SparqlLibSetAgg.wrap1(SparqlLibSetAgg::aggNodeSet));
    }

    public static void loadDefs(FunctionRegistry registry) {
        // Datatype is registered in the jenax-arq-datatype module!
        // TypeMapper.getInstance().registerDatatype(RDFDatatypeNodeSet.get());

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

        binder.registerAll(SparqlLibSetFn.class);

        PropertyFunctionRegistry.get().put(NS + "unnest", PF_CollectionUnnest.class);
        // PropertyFunctionRegistry.get().put(NS + "explode", PF_ArrayExplode.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("set", NS);
    }

}

