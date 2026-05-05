package org.aksw.jenax.sparql.qudtlib;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestQudtlib {
    static { JenaSystem.init(); }
    @Test
    public void testConvert() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:convert(38.5, unit:DEG_C, unit:DEG_F)", pm));
        Assertions.assertEquals(101.3, nv.getDouble(), 0.0005, "Conversion from C to F");
    }

    @Test
    public void testRegistered() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:registeredUnit(unit:DEG_F)", pm));
        Assertions.assertTrue(nv.getBoolean(), "F Unit is registered");
    }


    @Test
    public void testConvertible() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:convertible(unit:DEG_F, unit:M)", pm));
        Assertions.assertFalse(nv.getBoolean(), "Degree F is not convertible to Metres");
    }

    @Test
    public void testUnitFromLabel() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("qudt", Functions.NS);
//        pm.setNsPrefix("unit", "http://qudt.org/vocab/unit/");
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("qudt:unitFromLabel(\"Square Millimetre\")", pm));
        Assertions.assertEquals("http://qudt.org/vocab/unit/MilliM2", nv.getNode().getURI(), "The square millimetre unit is found by full English label");
    }
}
