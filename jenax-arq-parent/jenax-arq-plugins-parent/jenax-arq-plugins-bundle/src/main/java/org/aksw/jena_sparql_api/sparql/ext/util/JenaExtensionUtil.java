package org.aksw.jena_sparql_api.sparql.ext.util;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.fs.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.sparql.ext.sys.JenaExtensionSys;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionGenerator;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionRegistry;

import com.google.gson.JsonElement;

public class JenaExtensionUtil {
//    public static void registerAll() {
//        throw new RuntimeException("Not yet implemented, because we need to support configuration options");
//    }

    public static void addPrefixes(PrefixMapping pm) {
        JenaExtensionJson.addPrefixes(pm);
        JenaExtensionCsv.addPrefixes(pm);
        JenaExtensionXml.addPrefixes(pm);
        JenaExtensionUrl.addPrefixes(pm);
        JenaExtensionFs.addPrefixes(pm);
        JenaExtensionSys.addPrefixes(pm);
    }


    private static FunctionBinder DFT_FUNCTION_BINDER = null;

    public static FunctionBinder getDefaultFunctionBinder() {
        if (DFT_FUNCTION_BINDER == null) {
            synchronized (JenaExtensionUtil.class) {
                if (DFT_FUNCTION_BINDER == null) {
                    DFT_FUNCTION_BINDER = createFunctionBinder(FunctionRegistry.get());
                }
            }
        }
        return DFT_FUNCTION_BINDER;
    }



    public static FunctionBinder createFunctionBinder(FunctionRegistry functionRegistry) {
        FunctionBinder binder = new FunctionBinder(functionRegistry);
        FunctionGenerator generator = binder.getFunctionGenerator();

        // Define two-way Geometry - GeometryWrapper coercions
        generator.getConverterRegistry()
            .register(BigDecimal.class, Long.class,
                    BigDecimal::longValueExact, BigDecimal::new)
            .register(BigDecimal.class, Integer.class,
                    BigDecimal::intValueExact, BigDecimal::new)
            .register(BigDecimal.class, Short.class,
                    BigDecimal::shortValueExact, BigDecimal::new)
            .register(BigDecimal.class, Byte.class,
                    BigDecimal::byteValueExact, BigDecimal::new)
            .register(BigDecimal.class, Double.class,
                    BigDecimal::doubleValue, BigDecimal::new)
            .register(BigDecimal.class, Float.class,
                    BigDecimal::floatValue, BigDecimal::new)
            .register(Node.class, NodeValue.class,
                    NodeValue::makeNode, NodeValue::asNode)
            .register(Node.class, JsonElement.class,
                    RDFDatatypeJson::extract, RDFDatatypeJson::jsonToNode)
            ;


        return binder;
    }
}
