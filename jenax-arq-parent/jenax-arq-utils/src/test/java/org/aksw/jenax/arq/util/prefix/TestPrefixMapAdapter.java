package org.aksw.jenax.arq.util.prefix;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class TestPrefixMapAdapter {
    @Test
    public void test() {
        // PrefixMap pm = new org.apache.jena.riot.system.PrefixMapAdapter(PrefixMapping.Extended);
        PrefixMapping pmTrie = new PrefixMappingTrie();
        pmTrie.setNsPrefixes(PrefixMapping.Extended);


        // PrefixMap pm = new PrefixMapAdapter(PrefixMapping.Extended);;
        PrefixMap pm = new PrefixMapAdapter(pmTrie);;

        Assert.assertEquals("rdf:type", pm.abbreviate(RDF.type.getURI()));
        Assert.assertEquals("owl:Class", pm.abbreviate(OWL.Class.getURI()));
        Assert.assertEquals(null, pm.abbreviate(DCAT.Dataset.getURI()));
        Assert.assertEquals(null, pm.abbreviate(""));
        Assert.assertEquals("eg:test", pm.abbreviate("http://www.example.org/test"));
        Assert.assertEquals("eg:", pm.abbreviate("http://www.example.org/"));
        // TODO Add some more more corner cases with / and #
    }
}
