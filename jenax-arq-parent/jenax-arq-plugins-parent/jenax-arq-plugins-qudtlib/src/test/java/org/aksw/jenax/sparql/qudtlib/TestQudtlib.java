package org.aksw.jenax.sparql.qudtlib;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class TestQudtlib {
    static { JenaSystem.init(); }
    @Test
    public void testConvert() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:convert(38.5, unit:DEG_C, unit:DEG_F)", pm));
        System.out.println(nv);

    }

    @Test
    public void testRegistered() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:registeredUnit(unit:DEG_F)", pm));
        System.out.println(nv);

    }


    @Test
    public void testConvertible() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:convertible(unit:DEG_F, unit:M)", pm));
        System.out.println(nv);

    }

    @Test
    public void testUnitFromLabel() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
//        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:unitFromLabel(\"mm2\")", pm));
        System.out.println(nv);

    }
}
