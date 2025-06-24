package org.aksw.jenax.graphql.sparql;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderJava;
import org.junit.Assert;
import org.junit.Test;

public class TestGonJava
    extends TS_Gon<String, Object>
{
    public TestGonJava() {
        super(new GonProviderJava<>());
    }

    @Test
    public void test() {
        Object obj = provider.newObject();
        provider.setProperty(obj, "int", provider.newLiteral(1));

        Object arr = provider.newArray();
        provider.setProperty(obj, "array", arr);

        provider.addElement(arr, provider.newLiteral("foo"));
        provider.addElement(arr, provider.newLiteral(2));

        System.out.println(obj);
    }

    @Test
    public void testLiteral() {
        Object lit = provider.newLiteral("test");
        Assert.assertFalse(provider.isObject(lit));
        Assert.assertFalse(provider.isArray(lit));
        Assert.assertTrue(provider.isLiteral(lit));
        Assert.assertFalse(provider.isNull(lit));
    }
}

