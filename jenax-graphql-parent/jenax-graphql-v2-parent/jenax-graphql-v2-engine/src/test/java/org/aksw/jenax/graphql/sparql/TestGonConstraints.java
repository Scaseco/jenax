package org.aksw.jenax.graphql.sparql;

import org.aksw.jenax.graphql.sparql.v2.gon.meta.GonType;
import org.junit.Assert;
import org.junit.Test;

public class TestGonConstraints {
    @Test
    public void test() {
        // An array can be seen as both a non-object and a node-type.
        // Assert.assertTrue(GonCategory.NODE_TYPE.getMembers().contains(GonType.ARRAY.getRawType()));

        Assert.assertTrue(GonType.ARRAY.isValidChildOf(GonType.ARRAY));
        Assert.assertTrue(GonType.ARRAY.isValidChildOf(GonType.ENTRY));
        Assert.assertFalse(GonType.ARRAY.isValidChildOf(GonType.LITERAL));
        Assert.assertFalse(GonType.ARRAY.isValidChildOf(GonType.OBJECT));
        Assert.assertTrue(GonType.ARRAY.isValidChildOf(GonType.ROOT));
        Assert.assertTrue(GonType.ARRAY.isValidChildOf(GonType.UNKNOWN));

        Assert.assertFalse(GonType.ENTRY.isValidChildOf(GonType.ARRAY));
        Assert.assertFalse(GonType.ENTRY.isValidChildOf(GonType.ENTRY));
        Assert.assertFalse(GonType.ENTRY.isValidChildOf(GonType.LITERAL));
        Assert.assertTrue(GonType.ENTRY.isValidChildOf(GonType.OBJECT));
        Assert.assertFalse(GonType.ENTRY.isValidChildOf(GonType.ROOT));
        Assert.assertTrue(GonType.ENTRY.isValidChildOf(GonType.UNKNOWN));

        Assert.assertTrue(GonType.LITERAL.isValidChildOf(GonType.ARRAY));
        Assert.assertTrue(GonType.LITERAL.isValidChildOf(GonType.ENTRY));
        Assert.assertFalse(GonType.LITERAL.isValidChildOf(GonType.LITERAL));
        Assert.assertFalse(GonType.LITERAL.isValidChildOf(GonType.OBJECT));
        Assert.assertTrue(GonType.LITERAL.isValidChildOf(GonType.ROOT));
        Assert.assertTrue(GonType.LITERAL.isValidChildOf(GonType.UNKNOWN));

        Assert.assertTrue(GonType.OBJECT.isValidChildOf(GonType.ARRAY));
        Assert.assertTrue(GonType.OBJECT.isValidChildOf(GonType.ENTRY));
        Assert.assertFalse(GonType.OBJECT.isValidChildOf(GonType.LITERAL));
        Assert.assertFalse(GonType.OBJECT.isValidChildOf(GonType.OBJECT));
        Assert.assertTrue(GonType.OBJECT.isValidChildOf(GonType.ROOT));
        Assert.assertTrue(GonType.OBJECT.isValidChildOf(GonType.UNKNOWN));
    }
}
