package org.aksw.jenax.model.shacl;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.model.shacl.domain.PrefixDeclaration;
import org.aksw.jenax.model.shacl.domain.ShPrefixMapping;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class HasPrefixesTests {

    @Test
    public void testHasPrefixes() {
        ShPrefixMapping ps = ModelFactory.createDefaultModel().createResource().as(ShPrefixMapping.class);

        // Two views over the same data
        // Map<String, String> map = ps.getMap();
        Set<PrefixDeclaration> defs = ps.getPrefixDeclarations();

        ps.put("rdf", RDF.getURI());

        // RDFDataMgr.write(System.out, ps.getModel(), RDFFormat.TURTLE_PRETTY);

        Map<String, String> map = ps.getMap();

        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, defs.size());
    }
}
