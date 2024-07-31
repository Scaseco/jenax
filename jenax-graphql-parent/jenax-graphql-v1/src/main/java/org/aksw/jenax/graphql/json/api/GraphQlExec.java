package org.aksw.jenax.graphql.json.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** A GraphQl execution */
public interface GraphQlExec {
    Set<String> getDataProviderNames();
    GraphQlDataProvider getDataProvider(String name);

    /**
     * Return the list of data providers.
     * This corresponds to the top-level keys available in the data field { data: { } }
     */
    default List<GraphQlDataProvider> getDataProviders() {
        List<GraphQlDataProvider> result = getDataProviderNames().stream().map(this::getDataProvider).collect(Collectors.toList());
        return result;
    }
}
