package org.aksw.jenax.arq.util.prefix;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.apache.commons.collections4.trie.PatriciaTrie;

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
    // protected PrefixMap prefixMap = PrefixMapFactory.createForOutput();
    protected PatriciaTrie<String> prefixMap = new PatriciaTrie<>();
    protected Map<String, Name> shortToFull = new HashMap<>();
    protected Function<String, String> shortNameSanitizer;
    protected ToIntFunction<CharSequence> nameSplitter;

    public record Name(String shortName, String prefix, String ns, String localName) {}

    @Override
    public ShortNameMgr clone() {
        ShortNameMgr result = new ShortNameMgr();
        result.namespaceCounter = namespaceCounter;
        result.prefixMap = new PatriciaTrie<>(prefixMap); // PrefixMapFactory.create(prefixMap.getMapping());
        result.shortToFull = new HashMap<>(shortToFull);
        result.shortNameSanitizer = shortNameSanitizer;
        result.nameSplitter = nameSplitter;
        return result;
    }

    public ShortNameMgr() {
        this(null, null);
    }

    public ShortNameMgr(Function<String, String> shortNameSanitizer) {
        this(null, shortNameSanitizer);
    }

    public ShortNameMgr(ToIntFunction<CharSequence> nameSplitter, Function<String, String> shortNameSanitizer) {
        super();
        this.nameSplitter = Optional.ofNullable(nameSplitter).orElse(ShortNameMgr::simpleIriSplit);
        this.shortNameSanitizer = shortNameSanitizer;
    }

    public Name allocate(String iri, String label) {
        String matchKey = longestPrefixOf(prefixMap, iri);

        String prefix = null;
        String ns;
        String localName;
        if (matchKey != null) {
            ns = matchKey;
            prefix = prefixMap.get(matchKey);
            localName = iri.substring(matchKey.length());
        } else {
            int splitPoint = nameSplitter.applyAsInt(iri); // SplitIRI.splitpoint(iri);
            if (splitPoint < 0) {
                splitPoint = iri.length();
            }

            ns = iri.substring(0, splitPoint);
            localName = iri.substring(splitPoint);

            // There was no prefix so allocate one.
            prefix = allocPrefix(ns);
            prefixMap.put(ns, prefix);
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

        if (shortNameSanitizer != null) {
            baseName = shortNameSanitizer.apply(baseName);
        }

        Name result = null;
        String shortName = baseName;
        for (int i = 0; (result = shortToFull.get(shortName)) != null &&
                !(result.ns().equals(ns) && result.localName().equals(localName)); ++i) {
            shortName = baseName + i;
        }

        if (result == null) {
            result = new Name(shortName, prefix, ns, localName);
            shortToFull.put(shortName, result);
        }
        return result;
    }

    protected String allocPrefix(String ns) {
        // XXX Could use e.g. the first two letters afters of the host name
        return "ns" +  ++namespaceCounter;
    }

    public static int simpleIriSplit(CharSequence cs) {
        boolean acceptableCharSeen = false;
        int i = cs.length() - 1;
        for (; i >= 0; --i) {
            char c = cs.charAt(i);
            if (c == '/' || c == '#' || c == '.') {
                if (acceptableCharSeen) {
                    ++i;
                    break;
                }
            } else {
                acceptableCharSeen = true;
            }
        }
        return i;
    }

    /** Returns the longest key in the trie that is a prefix of `value`, or null if none. */
    private String longestPrefixOf(PatriciaTrie<?> prefixMap, String value) {
        if (prefixMap.isEmpty()) return null;

        String candidate;
        try {
            // approximate floorKey(value)
            candidate = prefixMap.headMap(value + Character.MAX_VALUE).lastKey();
        } catch (NoSuchElementException e) {
            return null;
        }

        while (candidate != null && !value.startsWith(candidate)) {
            SortedMap<String, ?> head = prefixMap.headMap(candidate); // strictly less than candidate
            if (head.isEmpty()) return null;
            candidate = head.lastKey();
        }
        return candidate;
    }

    public static void main(String[] args) {
        ShortNameMgr mgr = new ShortNameMgr();
        System.out.println(mgr.allocate("http://foo.bar/Foo"));
        System.out.println(mgr.allocate("http://foo.bar/Bar"));
    }
}
