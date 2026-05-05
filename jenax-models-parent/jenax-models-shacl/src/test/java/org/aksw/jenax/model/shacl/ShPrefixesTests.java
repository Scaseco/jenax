package org.aksw.jenax.model.shacl;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.model.shacl.domain.ShPrefixDeclaration;
import org.aksw.jenax.model.shacl.domain.ShPrefixMapping;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShPrefixesTests {

//    @Test
//    public void testSetOfLists() {
//        ShNodeShape nodeShape = ModelFactory.createDefaultModel().createResource().as(ShNodeShape.class);
//
//        Set<List<ShNodeShape>> ors = nodeShape.getOr();
//
//        System.out.println(ors.size());
//    }

    @Test
    public void testHasPrefixes() {
        ShPrefixMapping ps = ModelFactory.createDefaultModel().createResource().as(ShPrefixMapping.class);

        // Two views over the same data
        // Map<String, String> map = ps.getMap();
        Set<ShPrefixDeclaration> defs = ps.getPrefixDeclarations();

        ps.put("rdf", RDF.getURI());

        // RDFDataMgr.write(System.out, ps.getModel(), RDFFormat.TURTLE_PRETTY);

        Map<String, String> map = ps.getMap();

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, defs.size());
    }
}
