package org.aksw.jenax.arq.util.prefix;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.ext.com.google.common.cache.CacheBuilder;
import org.apache.jena.riot.system.PrefixLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;
import org.apache.jena.sparql.graph.PrefixMappingBase;

/**
 * A {@link PrefixMap} implemented as a pair of in-memory maps.
 * In addition, internal longest prefix lookups are cached.
 *
 * Prefixes are stored in a synchronized linked hash map such that their iteration order is predictable.
 */
public class PrefixMapTrie extends PrefixMapBase {

    private Map<String, String> prefixToIri = Collections.synchronizedMap(new LinkedHashMap<>());
    private Map<String, String> prefixToIriView = Collections.unmodifiableMap(prefixToIri);

    private Trie<String> iriToPrefix = new Trie<>();

    /** Wrapping with Optional needed because Guava cache does not allow for null values */
    private Cache<String, Optional<String>> longestMatchCache;

    public PrefixMapTrie() {
        this(1000);
    }

    public PrefixMapTrie(long longestMatchCacheSize) {
        super();
        this.longestMatchCache = CacheBuilder.newBuilder().maximumSize(longestMatchCacheSize).build();
    }

    public Optional<String> findMapping(String iri, boolean partial) {
        Optional<String> prefix;
        try {
            prefix = partial
                    ? longestMatchCache.get(iri, () -> Optional.ofNullable(iriToPrefix.longestMatch(iri)))
                    : Optional.ofNullable(iriToPrefix.get(iri));
        } catch (ExecutionException e) {
            throw new RuntimeException("Unexpected failure during cache lookup", e);
        }

        return prefix;
    }

    @Override
    public void add(String prefix, String iri) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(iri);
        String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
        longestMatchCache.invalidateAll();
        String oldIri = prefixToIri.get(canonicalPrefix);
        if (oldIri != null) {
            iriToPrefix.remove(oldIri);
        }
        prefixToIri.put(canonicalPrefix, iri);
        iriToPrefix.add(iri, canonicalPrefix);
    }

    /** See notes on reverse mappings in {@link PrefixMappingBase}.
     * This is a complete implementation.
     * <p>
     * Test {@code AbstractTestPrefixMapping.testSecondPrefixDeletedUncoversPreviousMap}.
     */
    @Override
    public void delete(String prefix) {
        Objects.requireNonNull(prefix);
        String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
        longestMatchCache.invalidateAll();
        String iriForPrefix = prefixToIri.get(canonicalPrefix);
        if (iriForPrefix != null) {
            prefixToIri.remove(canonicalPrefix);
            String prefixForIri = iriToPrefix.get(iriForPrefix);
            if (canonicalPrefix.equals(prefixForIri)) {
                iriToPrefix.remove(prefixForIri);
            }
        }
    }

    @Override
    public Pair<String, String> abbrev(String iriStr) {
        Objects.requireNonNull(iriStr);
        Pair<String, String> result = null;
        String iriForPrefix = null;
        String candidate = getPossibleKey(iriStr);

        // Try fast track first
        if (candidate != null) {
            iriForPrefix = iriToPrefix.get(candidate);
        }

        // If no solution yet then search for longest prefix
        if (iriForPrefix == null) {
            String prefix = findMapping(iriStr, true).orElse(null);
            if (prefix != null) { // Robustness; actually null keys should not exists
                iriForPrefix = prefixToIri.get(prefix);
            }
        }

        // Post process a found solution
        if (iriForPrefix != null) {
            String localName = iriStr.substring(iriForPrefix.length());
            if (PrefixLib.isSafeLocalPart(localName)) {
                result = Pair.create(iriForPrefix, localName);
            }
        }
        return result;
    }

    @Override
    public String abbreviate(String iriStr) {
        Objects.requireNonNull(iriStr);

        String result = null;
        Pair<String, String> prefixAndLocalName = abbrev(iriStr);
        if (prefixAndLocalName != null) {
            String prefix = prefixAndLocalName.getLeft();
            String ln = prefixAndLocalName.getRight();
            // Safe for RDF/XML as well
            if (strSafeFor(ln, ':')) {
                result = prefix + ":" + ln;
            }
        }
        return result;
    }

    @Override
    public String get(String prefix) {
        Objects.requireNonNull(prefix);
        return prefixToIri.get(prefix);
    }

    @Override
    public Map<String, String> getMapping() {
        return prefixToIriView;
    }

    @Override
    public void clear() {
        longestMatchCache.invalidateAll();
        prefixToIri.clear();
        iriToPrefix.clear();
    }

    @Override
    public boolean isEmpty() {
        return prefixToIri.isEmpty();
    }

    @Override
    public int size() {
        return prefixToIri.size();
    }

    @Override
    public boolean containsPrefix(String prefix) {
        Objects.requireNonNull(prefix);
        String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
        return prefixToIri.containsKey(canonicalPrefix);
    }

    protected static String getPossibleKey(String iriString) {
        int n = iriString.length();
        int i;
        for (i = n - 1; i >= 0; --i) {
            char c = iriString.charAt(i);
            if (c == '#' || c == '/') {
                // We could add ':' here, it is used as a separator in URNs.
                // But it is a multiple use character and always present in the scheme name.
                // This is a fast-track guess so don't try guessing based on ':'.
                break;
            }
        }

        String result = i >= 0 ? iriString.substring(0, i + 1) : null;
        return result;
    }
}
