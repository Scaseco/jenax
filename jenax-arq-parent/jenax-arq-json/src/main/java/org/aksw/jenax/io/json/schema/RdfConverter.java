package org.aksw.jenax.io.json.schema;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

public interface RdfConverter<T> {
    T convert(Graph graph, Node node);
}
