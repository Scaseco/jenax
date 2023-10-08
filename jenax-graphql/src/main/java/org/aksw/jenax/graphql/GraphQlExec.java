package org.aksw.jenax.graphql;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** A GraphQl execution */
public interface GraphQlExec {
    /** Get the top-level keys available in the data field { data: { } } */
    Set<String> getDataStreamNames();

    /** Obtained streams must be closed otherwise resources may be leaked! */
    // Stream<JsonElement> getDataStream(String name);
    GraphQlStream getDataStream(String name);

    default List<GraphQlStream> getDataStreams() {
        return getDataStreamNames().stream().map(this::getDataStream).collect(Collectors.toList());
    }
}
