package org.aksw.jenax.arq.functionbinder;

import java.util.Arrays;
import java.util.function.Supplier;

import org.aksw.commons.util.convert.ConverterRegistries;
import org.aksw.commons.util.convert.ConverterRegistry;
import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionRegistry;

public class FunctionBinders {
    private static FunctionBinder DFT_FUNCTION_BINDER = null;

    public static FunctionBinder getDefaultFunctionBinder() {
        if (DFT_FUNCTION_BINDER == null) {
            synchronized (FunctionBinders.class) {
                if (DFT_FUNCTION_BINDER == null) {
                    DFT_FUNCTION_BINDER = createFunctionBinder(FunctionRegistry::get);
                }
            }
        }
        return DFT_FUNCTION_BINDER;
    }

    public static FunctionBinder createFunctionBinder(Supplier<FunctionRegistry> functionRegistrySupplier) {
        FunctionBinder binder = new FunctionBinder(functionRegistrySupplier);
        FunctionGenerator generator = binder.getFunctionGenerator();

        // This declaration states that whenever CharSequence is demanded (e.g. as a method  parameter)
        // then any RDF Datatype backed by a Java String class qualifies
        generator.getJavaToRdfTypeMap().put(CharSequence.class, String.class);

        ConverterRegistry converterRegistry = generator.getConverterRegistry();
        ConverterRegistries.addDefaultConversions(converterRegistry);
        addNodeConversions(converterRegistry);
        return binder;
    }

    /** Register Node-to-NodeValue and Node[]-to-NodeList conversions */
    public static void addNodeConversions(ConverterRegistry converterRegistry) {
        converterRegistry
            .register(Node.class, NodeValue.class,
                    NodeValue::makeNode, NodeValue::asNode)
            .register(Node[].class, NodeList.class,
                    arr -> new NodeListImpl(Arrays.asList(arr)), list -> list.toArray(new Node[0]))
            ;
    }
}
