package org.aksw.jenax.graphql.schema.generator;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.util.SplitIRI;

/** Allocate short name for IRIs. By default this is the IRI's localName but conflicts are resolved. */
public class ShortNameMgr {
    protected int namespaceCounter = 0;
    protected PrefixMap prefixMap = PrefixMapFactory.createForOutput();
    protected Map<String, Name> shortToFull = new HashMap<>();

    record Name(String shortName, String prefix, String ns, String localName) {}

    public Name allocate(String iri) {
        Pair<String, String> pair = prefixMap.abbrev(iri);
        String prefix;
        String ns;
        String localName;
        if (pair != null) {
            // ns = pair.getLeft();
            prefix = pair.getLeft();
            ns = prefixMap.get(prefix);
            localName = pair.getRight();
        } else {
            int splitPoint = SplitIRI.splitXML(iri);
            if (splitPoint < 0) {
                splitPoint = iri.length();
            }

            ns = iri.substring(0, splitPoint);
            localName = iri.substring(splitPoint);
        }

        String baseName = localName;

        if (baseName.isEmpty()) {
            baseName = "_";
        }

        String shortName = baseName;

        Name result = null;
        for (int i = 0; (result = shortToFull.get(shortName)) != null && !result.ns().equals(ns); ++i) {
            shortName = baseName + i;
        }

        if (result == null) {
            prefix = allocPrefix(ns);
            prefixMap.add(prefix, ns);
            result = new Name(shortName, prefix, ns, localName);
            shortToFull.put(shortName, result);
        }
        return result;
    }

    protected String allocPrefix(String ns) {
        // XXX Could use e.g. the first two letters afters of the host name
        return "ns" +  ++namespaceCounter;
    }
}
