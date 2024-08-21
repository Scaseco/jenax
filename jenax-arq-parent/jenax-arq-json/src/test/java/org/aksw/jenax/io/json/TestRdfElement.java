package org.aksw.jenax.io.json;

import org.aksw.jenax.ron.ParentLink;
import org.aksw.jenax.ron.ParentLinkObject;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfLiteralImpl;
import org.aksw.jenax.ron.RdfObject;
import org.aksw.jenax.ron.RdfObjectImpl;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public class TestRdfElement {

    @Test
    public void testObjectParentLink() {
        RdfObject obj = new RdfObjectImpl();
        RdfElement v = new RdfLiteralImpl(NodeFactory.createLiteralString("test"));
        P_Path0 p = new P_Link(RDFS.label.asNode());
        obj.add(p, v);

        ParentLink parentLink = v.getParent();
        Assert.assertTrue(parentLink.isObjectLink());

        ParentLinkObject objLink = parentLink.asObjectLink();

        Assert.assertEquals(obj, objLink.getParent());
        Assert.assertEquals(p, objLink.getKey());



        // obj.as
    }
}
