package org.aksw.jenax.sparql.qudtlib;

import java.math.BigDecimal;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sys.JenaSystem;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.model.Unit;

public class Functions {
    static {
        JenaSystem.init();
    }
    public static final String NS = "http://jsa.aksw.org/fn/qudtlib/";

    @IriNs(NS)
    public static BigDecimal convert(BigDecimal value, Unit from, Unit to) {
        return Qudt.convert(value, from, to);
    }

    @IriNs(NS)
    public static boolean isConvertible(Unit from, Unit to) {
        return Qudt.isConvertible(from, to);
    }

    @IriNs(NS)
    public static boolean isRegisteredUnit(Node node) {
        return node.isURI() && Qudt.unit(node.getURI()).isPresent();
    }

    @IriNs(NS)
    public static Unit unitFromLabel(String label) {
        return Qudt.unitFromLabel(label).orElse(null);
    }

    public static void register() {
        FunctionRegistry functionRegistry = FunctionRegistry.get();
        FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
        binder.getFunctionGenerator().getConverterRegistry().register(Unit.class, Node.class,
                unit -> NodeFactory.createURI(unit.getIri()),
                node -> Qudt.unit(node.getURI()).orElseThrow(() -> new NoSuchUnitException(node)));
        binder.getFunctionGenerator().getJavaToRdfTypeMap().put(Unit.class, Node.class);
        binder.registerAll(Functions.class);
    }
}
