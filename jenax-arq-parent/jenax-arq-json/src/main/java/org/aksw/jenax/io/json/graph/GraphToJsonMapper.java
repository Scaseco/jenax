package org.aksw.jenax.io.json.graph;

import org.aksw.commons.path.json.PathJson;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public interface GraphToJsonMapper {
    // TODO errors should be a callback that receives the events
    JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node);
}

