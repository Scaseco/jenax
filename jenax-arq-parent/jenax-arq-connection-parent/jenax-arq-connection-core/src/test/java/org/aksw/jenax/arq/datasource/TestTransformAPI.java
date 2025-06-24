package org.aksw.jenax.arq.datasource;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.op.OpTransformList;
import org.aksw.jenax.arq.util.query.OpTransformBuilder;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.query.QueryTransformBuilder;
import org.aksw.jenax.arq.util.query.QueryTransformFromOpTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransformBuilder;
import org.apache.jena.sparql.algebra.Op;
import org.junit.Assert;
import org.junit.Test;

public class TestTransformAPI {
    @Test
    public void testOpTransform() {
        OpTransformBuilder builder = new OpTransformBuilder();
        OpTransform xform = builder.build();
        Assert.assertNull(xform); // Empty builder should return null

        builder.add(op -> op);
        xform = builder.build();

        builder.add(op -> op);
        xform = builder.build();

        OpTransformList list = (OpTransformList)xform;
        Assert.assertEquals(2, list.getMods().size());
    }

    @Test
    public void testQueryTransform() {
        QueryTransformBuilder builder = new QueryTransformBuilder();
        QueryTransform xform = builder.build();
        Assert.assertNull(xform); // Empty builder should return null

        builder.add((Op op) -> op);
        xform = builder.build();
        Assert.assertEquals(QueryTransformFromOpTransform.class, xform.getClass());

        builder.add((Op op) -> op);
        xform = builder.build();

        QueryTransformFromOpTransform x = (QueryTransformFromOpTransform)xform;
        OpTransformList list = (OpTransformList)x.getOpTransform();
        Assert.assertEquals(2, list.getMods().size());

        // Adding the generated transform to the builder should double the
        // number of op transforms
        builder.add(xform);
        xform = builder.build();
        x = (QueryTransformFromOpTransform)xform;
        list = (OpTransformList)x.getOpTransform();
        Assert.assertEquals(4, list.getMods().size());
    }

    @Test
    public void testLinkSparqlQueryTransform() {
        LinkSparqlQueryTransformBuilder builder = new LinkSparqlQueryTransformBuilder();
        // builder.add(null)
        builder.add((Op op) -> op);
        builder.add((Op op) -> op);

        LinkSparqlQueryTransform xform = builder.build();
        // TODO finish
        // System.out.println(xform);
    }
}
