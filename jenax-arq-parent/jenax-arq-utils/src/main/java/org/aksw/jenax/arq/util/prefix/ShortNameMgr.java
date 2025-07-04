package org.aksw.jenax.arq.util.prefix;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.util.SplitIRI;

/**
 * Allocate short name for IRIs.
 * By default this is the IRI's localName but conflicts are resolved.
 * The manager guarantees uniqueness of generated short names.
 */
// XXX Probably a two-phase approach would be better:
//   phase 1: collect all names and conflicts
//   phase 2: resolve conflicts.
public class ShortNameMgr {
    protected int namespaceCounter = 0;
    protected PrefixMap prefixMap = PrefixMapFactory.createForOutput();
    protected Map<String, Name> shortToFull = new HashMap<>();
    protected Function<String, String> shortNameSanitizer;

    public record Name(String shortName, String prefix, String ns, String localName) {}

    public ShortNameMgr() {
        this(null);
    }

    public ShortNameMgr(Function<String, String> shortNameSanitizer) {
        super();
        this.shortNameSanitizer = shortNameSanitizer;
    }

    public Name allocate(String iri, String label) {
        Pair<String, String> pair = prefixMap.abbrev(iri);
        String prefix = null;
        String ns;
        String localName;
        if (pair != null) {
            // ns = pair.getLeft();
            prefix = pair.getLeft();
            ns = prefixMap.get(prefix);
            localName = pair.getRight();
        } else {
            int splitPoint = SplitIRI.splitpoint(iri); // .splitXML(iri);
            if (splitPoint < 0) {
                splitPoint = iri.length();
            }

            ns = iri.substring(0, splitPoint);
            localName = iri.substring(splitPoint);
        }

        // XXX Hacky - shouldn't conflate label and localName.
        if (label != null) {
            localName = label;
        }

        return allocate(prefix, ns, localName);
    }

    public Name allocate(String iri) {
        return allocate(iri, null);
    }

    protected Name allocate(String prefix, String ns, String localName) {
        String baseName = localName;

        if (baseName.isEmpty()) {
            baseName = "_";
        }

        String shortName = shortNameSanitizer == null
            ? baseName
            : shortNameSanitizer.apply(baseName);

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
