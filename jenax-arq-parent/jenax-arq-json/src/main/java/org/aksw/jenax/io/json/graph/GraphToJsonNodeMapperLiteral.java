package org.aksw.jenax.io.json.graph;

import org.aksw.commons.path.json.PathJson;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GraphToJsonNodeMapperLiteral
    implements GraphToJsonNodeMapper
{
    private static final GraphToJsonNodeMapperLiteral INSTANCE = new GraphToJsonNodeMapperLiteral();

    public static GraphToJsonNodeMapperLiteral get() {
        return INSTANCE;
    }

    protected GraphToJsonNodeMapperLiteral() {
        super();
    }

    @Override
    public GraphToJsonNodeMapperType getType() {
        return GraphToJsonNodeMapperType.LITERAL;
    }

    @Override
    public JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node) {
        JsonElement result = RdfJsonUtils.toJson(graph, node, 0, 1, false);
        return result;
    }

    @Override
    public String toString() {
        return "NodeMapperLiteral []";
    }
}
