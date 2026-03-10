package org.aksw.jenax.arq.util.syntax;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;

/**
 * This class is just a sketch without working functionality!
 * The intent is to finally build an index structure on top of {@link QueryHash}
 * (and evolve the hash as necessary).
 */
public class QueryIndex<T> {
    private Map<String, Map<Query, T>> prefixToEntries = new HashMap<>();

    /**
     * Record for how to transform the indexed query into the lookup query.
     * Rename variables, project the selected variables.
     *
     * @param <T>
     */
    record Match<T>(Query query, T value, Map<Var, Var> varMap, List<Var> projection) {}

    public void put(Query query, T value) {
        QueryHash hash = QueryHash.createHash(query);
        System.out.println("relabel" + hash.getVarMapCanonicalToOriginal());
        System.out.println("relabel" + hash.getVarMapOriginalToCanonical());
        // Query indexQuery = hash.getHarmonizedQuery();

        String identityHash = QueryHash.getIdentityHash(hash);

        Map<Query, T> map = prefixToEntries.computeIfAbsent(identityHash, k -> new LinkedHashMap<>());
    }

    public Stream<Match<T>> find() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public static void main(String[] args) {
        QueryIndex index = new QueryIndex<>();
        index.put(QueryFactory.create("SELECT * { ?s ?p ?o }"), "foo");
        index.put(QueryFactory.create("SELECT * { ?a ?b ?c }"), "bar");
    }
}



