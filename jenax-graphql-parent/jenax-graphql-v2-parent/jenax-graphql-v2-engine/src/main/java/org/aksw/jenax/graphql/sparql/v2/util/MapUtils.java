package org.aksw.jenax.graphql.sparql.v2.util;

import java.util.Map;
import java.util.function.BinaryOperator;

import graphql.com.google.common.collect.Maps;
import graphql.com.google.common.collect.Sets;

public class MapUtils {
    /**
     * Returns a view of the union of two maps.
     *
     * For all keys k, if either map contains a value for k, the returned map contains that value. If both maps
     * contain a value for the same key, this map contains the value in the second of the two provided maps.
     */
    public static <K, V> Map<K, V> union(Map<K, ? extends V> a, Map<K, ? extends V> b) {
        return union(a, b, (v1, v2) -> v2);
    }

    /**
     * Returns a view of the union of two maps.
     *
     * For all keys k, if either map contains a value for k, the returned map contains that value. If both maps
     * contain a value for the same key, the conflict is resolved with the provided function.
     */
    public static <K, V> Map<K, V> union(
            Map<K, ? extends V> a,
            Map<K, ? extends V> b,
            BinaryOperator<V> conflictHandler) {
        return Maps.asMap(Sets.union(a.keySet(), b.keySet()),
                (K k) -> {
                    V r;
                    if (!a.containsKey(k)) {
                        r = b.get(k);
                    } else if (!b.containsKey(k)) {
                        r = a.get(k);
                    } else {
                        V v1 = a.get(k);
                        V v2 = b.get(k);
                        r = conflictHandler.apply(v1, v2);
                    }
                    return r;
                });
    }

}
