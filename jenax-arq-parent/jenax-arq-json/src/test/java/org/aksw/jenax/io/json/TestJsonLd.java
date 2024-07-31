package org.aksw.jenax.io.json;

import org.aksw.jenax.io.rdf.json.JsonLdContext;
import org.aksw.jenax.io.rdf.jsonld.JsonLdTerms;
import org.aksw.jenax.ron.RdfObject;
import org.aksw.jenax.ron.RdfObjectImpl;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public class TestJsonLd {

    public static RdfObject createTestDocument() {
        RdfObject c1 = new RdfObjectImpl();
        c1.addForward("rdf", RDF.uri);

        RdfObject c2 = new RdfObjectImpl();
        c1.addForward("rdfs", RDFS.uri);

        RdfObject o1 = new RdfObjectImpl();
        o1.addStr(JsonLdTerms.context, c1);

        RdfObject o2 = new RdfObjectImpl();
        o2.addStr(JsonLdTerms.context, c2);

        o1.addForward(FOAF.knows, o2);

        return o1;
    }

    @Test
    public void testNamespaceLookup() {
        RdfObject o1 = createTestDocument();
        RdfObject o2 = o1.get(FOAF.knows).getAsObject();

        String ns = JsonLdContext.getNamespaceIri(o2, "rdf");

        Assert.assertEquals(RDF.uri, ns);
    }
}
