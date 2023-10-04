package org.aksw.jenax.io.json.mapper;

import org.aksw.commons.path.json.PathJson;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public interface RdfToJsonMapper
{
    JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node);
}

