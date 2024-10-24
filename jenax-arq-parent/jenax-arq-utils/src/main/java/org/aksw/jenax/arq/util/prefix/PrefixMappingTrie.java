package org.aksw.jenax.arq.util.prefix;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Trie;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.graph.PrefixMappingBase;

/**
 * A {@link PrefixMapping} implemented as a pair of in-memory maps.
 *
 * @implNote
 * {@link PrefixMappingImpl} is the long time implementation.
 * This class should be exactly the same within the {@link PrefixMappingBase} framework.
 */
public class PrefixMappingTrie extends PrefixMappingBase {

    private Map<String, String> prefixToUri = new ConcurrentHashMap<>();
    private Trie<String> uriToPrefix = new Trie<>();

    private Cache<String, Optional<String>> longestMatchCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    public PrefixMappingTrie() {}

    @Override
    public Optional<Entry<String, String>> findMapping(String uri, boolean partial ) {
        String prefix;
        try {
            prefix = partial
                    ? longestMatchCache.get(uri, () -> Optional.ofNullable(uriToPrefix.longestMatch(uri))).orElse(null)
                    : uriToPrefix.get(uri);
        } catch (ExecutionException e) {
            throw new RuntimeException("Unexpected failure during cache lookup", e);
        }

        return Optional.ofNullable(prefix).map(p -> new SimpleEntry<>(p, prefixToUri.get(p)));
    }

    @Override
    protected void add(String prefix, String uri) {
        longestMatchCache.invalidateAll();
        prefixToUri.put(prefix, uri);
        uriToPrefix.add(uri, prefix);
    }

    /** See notes on reverse mappings in {@link PrefixMappingBase}.
     * This is a complete implementation.
     * <p>
     * Test {@code AbstractTestPrefixMapping.testSecondPrefixDeletedUncoversPreviousMap}.
     */
    @Override
    protected void remove(String prefix) {
        longestMatchCache.invalidateAll();
        String u = prefixToUri(prefix);
        if ( u == null )
            return;
        String p = findReverseMapping(u, prefix);
        prefixToUri.remove(prefix);
        uriToPrefix.remove(u);
        // Reverse mapping.
        if ( p != null )
            uriToPrefix.remove(u);
    }

    // Find a prefix for a uri that isn't the supplied prefix.
    protected String findReverseMapping(String uri, String prefixExclude) {
        Objects.requireNonNull(prefixExclude);
        for (Map.Entry<String, String> e : prefixToUri.entrySet() ) {
            String p = e.getKey();
            String u = e.getValue();
            if (uri.equals(u) && !prefixExclude.equals(p)) {
                return p;
            }
        }
        return null;
    }

    @Override
    protected void clear() {
        longestMatchCache.invalidateAll();
        prefixToUri.clear();
        uriToPrefix.clear();
    }

    @Override
    protected boolean isEmpty() {
        return prefixToUri.isEmpty();
    }

    @Override
    protected int size() {
        return prefixToUri.size();
    }

    @Override
    protected String prefixToUri(String prefix) {
        return prefixToUri.get(prefix);
    }

    @Override
    protected String uriToPrefix(String uri) {
        return uriToPrefix.get(uri);
    }

    @Override
    protected Map<String, String> asMap() {
        return prefixToUri;
    }

    @Override
    protected Map<String, String> asMapCopy() {
        return new HashMap<>(prefixToUri);
    }

    @Override
    protected void apply(BiConsumer<String, String> action) {
        prefixToUri.forEach(action);
    }
}