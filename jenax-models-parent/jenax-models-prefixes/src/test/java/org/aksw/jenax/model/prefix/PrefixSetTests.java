package org.aksw.jenax.model.prefix;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.model.prefix.domain.api.PrefixDefinition;
import org.aksw.jenax.model.prefix.domain.api.PrefixSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;


public class PrefixSetTests {

    @Test
    public void test() {
        PrefixSet ps = ModelFactory.createDefaultModel().createResource().as(PrefixSet.class);

        // Two views over the same data
        // Map<String, String> map = ps.getMap();
        Set<PrefixDefinition> defs = ps.getDefinitions();

        ps.put("rdf", RDF.getURI());

        // RDFDataMgr.write(System.out, ps.getModel(), RDFFormat.TURTLE_PRETTY);

        Map<String, String> map = ps.getMap();

        Assert.assertEquals(1, map.size());
        Assert.assertEquals(1, defs.size());
    }
}
