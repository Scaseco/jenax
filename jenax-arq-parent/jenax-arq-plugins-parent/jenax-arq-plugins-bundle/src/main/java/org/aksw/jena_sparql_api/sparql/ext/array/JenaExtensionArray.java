package org.aksw.jena_sparql_api.sparql.ext.array;

import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionArray {
    public static final String NS = "http://jsa.aksw.org/fn/array/";


    public static void register() {
        loadDefs(FunctionRegistry.get());

        AggregateRegistry.register(
                NS + "collect",
                SparqlLibArrayAgg.wrap1(SparqlLibArrayAgg::aggNodeList));
    }

    public static void loadDefs(FunctionRegistry registry) {
        TypeMapper.getInstance().registerDatatype(RDFDatatypeNodeList.INSTANCE);

        FunctionBinder binder = JenaExtensionUtil.getDefaultFunctionBinder();
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

        PropertyFunctionRegistry.get().put(NS + "unnest", new PFF_ArrayUnnest());
        PropertyFunctionRegistry.get().put(NS + "explode", PF_ArrayExplode.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("array", NS);
    }

}

