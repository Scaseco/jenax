package org.aksw.jenax.io.json.mapper;

import org.aksw.commons.path.json.PathJson;
import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class RdfToJsonNodeMapperLiteral
    implements RdfToJsonNodeMapper
{
    private static final RdfToJsonNodeMapperLiteral INSTANCE = new RdfToJsonNodeMapperLiteral();

    public static RdfToJsonNodeMapperLiteral get() {
        return INSTANCE;
    }

    protected RdfToJsonNodeMapperLiteral() {
        super();
    }

    @Override
    public RdfToJsonNodeMapperType getType() {
        return RdfToJsonNodeMapperType.LITERAL;
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
