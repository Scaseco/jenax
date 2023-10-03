package org.aksw.jenax.io.json.schema;

import org.aksw.jenax.arq.json.RdfJsonUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonElement;

public class NodeConverterLiteral
    implements NodeConverter
{
    private static final NodeConverterLiteral INSTANCE = new NodeConverterLiteral();

    public static NodeConverterLiteral get() {
        return INSTANCE;
    }

    protected NodeConverterLiteral() {
        super();
    }

    @Override
    public RdfToJsonConverterType getType() {
        return RdfToJsonConverterType.LITERAL;
    }

    @Override
    public JsonElement convert(Graph graph, Node node) {
        JsonElement result = RdfJsonUtils.toJson(graph, node, 0, 1, false);
        return result;
    }

    @Override
    public String toString() {
        return "NodeConverterLiteral []";
    }
}
