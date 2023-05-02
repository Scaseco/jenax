package org.aksw.jenax.arq.functionbinder;

import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
import org.junit.Test;

public class TestFunctionBinder {

    // TODO Needed again as of Jena 4.7.0-SNAPSHOT 2022-12-16
    static { JenaSystem.init(); }

    @Test
    public void test() throws Exception {
        FunctionBinder binder = new FunctionBinder();
        binder.register(TestFunctionBinder.class.getMethod("myFn", String.class, int.class, float.class));

        NodeValue nv = ExprUtils.eval(ExprUtils.parse("<urn:test>('world', 2)"));
        String str = nv.asUnquotedString();

        Assert.assertEquals("hello world - 2 - 3.14", str);
    }

    // @Test
    public void test2() throws Exception {
        FunctionBinder binder = new FunctionBinder();
        binder.register(TestFunctionBinder.class.getMethod("myFn", String.class, int.class, float.class));

        // FIXME In debug mode we don't get ExprEvalExceptions because those have the stack trace disabled
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("<urn:test>('world', 'invalid type')"));
//		String str = nv.asUnquotedString();
//
//		Assert.assertEquals("hello world - 2 - 3.14", str);
    }

    @Iri("urn:test")
    public static String myFn(String arg, @DefaultValue("1") int intVal, @DefaultValue("3.14") float floatVal) {
        return "hello " + arg + " - " + intVal + " - " + floatVal;
    }
}
