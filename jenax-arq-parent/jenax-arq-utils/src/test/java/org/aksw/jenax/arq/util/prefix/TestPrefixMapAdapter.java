package org.aksw.jenax.arq.util.prefix;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPrefixMapAdapter {
    @Test
    public void test() {
        // PrefixMap pm = new org.apache.jena.riot.system.PrefixMapAdapter(PrefixMapping.Extended);
        PrefixMapping pmTrie = new PrefixMappingTrie();
        pmTrie.setNsPrefixes(PrefixMapping.Extended);


        // PrefixMap pm = new PrefixMapAdapter(PrefixMapping.Extended);;
        PrefixMap pm = new PrefixMapAdapter(pmTrie);;

        assertEquals("rdf:type", pm.abbreviate(RDF.type.getURI()));
        assertEquals("rdfs:subClassOf", pm.abbreviate(RDFS.subClassOf.getURI()));
        assertEquals("owl:Class", pm.abbreviate(OWL.Class.getURI()));
        assertEquals(null, pm.abbreviate(DCAT.Dataset.getURI()));
        assertEquals(null, pm.abbreviate(""));
        assertEquals("eg:test", pm.abbreviate("http://www.example.org/test"));
        assertEquals("eg:", pm.abbreviate("http://www.example.org/"));
        // TODO Add some more more corner cases with / and #
    }
}
