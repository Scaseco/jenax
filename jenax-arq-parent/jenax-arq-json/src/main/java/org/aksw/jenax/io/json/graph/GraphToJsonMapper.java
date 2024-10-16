package org.aksw.jenax.io.json.graph;

import org.aksw.commons.path.json.PathJson;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Interface for mapping RDF graphs to tree structures.
 *
 * FIXME Should be rename to something like GraphToTreeMapper.
 *
 * FIXME Currently the interface is tied to JsonElement, but it should be generalized to RdfElement
 *   which is an RDF-centric superset of JSON.
 */
public interface GraphToJsonMapper {
    // TODO errors should be a callback that receives the events; this API appends errors to the array
    JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node);
}

