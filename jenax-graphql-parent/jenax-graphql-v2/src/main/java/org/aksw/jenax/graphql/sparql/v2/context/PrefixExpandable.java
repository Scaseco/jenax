package org.aksw.jenax.graphql.sparql.v2.context;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

public interface PrefixExpandable<T> {

    default T expand(PrefixMap prefixMap) {
        PrefixMapping pm = new PrefixMappingAdapter(prefixMap);
        T result = expand(pm);
        return result;
    }

    T expand(PrefixMapping pm);
}
