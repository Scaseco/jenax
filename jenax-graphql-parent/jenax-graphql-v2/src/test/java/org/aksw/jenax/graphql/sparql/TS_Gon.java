package org.aksw.jenax.graphql.sparql;

import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProvider;
import org.junit.Assert;
import org.junit.Test;

public abstract class TS_Gon<K, V> {
    protected GonProvider<K, V> provider;

    public TS_Gon(GonProvider<K, V> provider) {
        super();
        this.provider = Objects.requireNonNull(provider);
    }

    @Test
    public void testObject() {
        Object obj = provider.newObject();
        Assert.assertTrue(provider.isObject(obj));
        Assert.assertFalse(provider.isArray(obj));
        Assert.assertFalse(provider.isLiteral(obj));
        Assert.assertFalse(provider.isNull(obj));
    }

    @Test
    public void testArray() {
        Object arr = provider.newArray();
        Assert.assertFalse(provider.isObject(arr));
        Assert.assertTrue(provider.isArray(arr));
        Assert.assertFalse(provider.isLiteral(arr));
        Assert.assertFalse(provider.isNull(arr));
    }

    @Test
    public void testNull() {
        Object nil = provider.newNull();
        Assert.assertFalse(provider.isObject(nil));
        Assert.assertFalse(provider.isArray(nil));
        Assert.assertFalse(provider.isLiteral(nil));
        Assert.assertTrue(provider.isNull(nil));
    }
}
