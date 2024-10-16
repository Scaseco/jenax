package org.aksw.jenax.graphql.rdf.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** A GraphQl execution */
public interface RdfGraphQlExec {
    Set<String> getDataProviderNames();
    RdfGraphQlDataProvider getDataProvider(String name);

    /**
     * Return the list of data providers.
     * This corresponds to the top-level keys available in the data field { data: { } }
     */
    default List<RdfGraphQlDataProvider> getDataProviders() {
        List<RdfGraphQlDataProvider> result = getDataProviderNames().stream().map(this::getDataProvider).collect(Collectors.toList());
        return result;
    }
}
