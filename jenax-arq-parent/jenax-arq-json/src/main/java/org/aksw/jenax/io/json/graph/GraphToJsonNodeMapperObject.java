package org.aksw.jenax.io.json.graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.path.json.PathJson.Step;
import org.aksw.jenax.io.json.accumulator.AggJsonNode;
import org.aksw.jenax.io.json.accumulator.AggJsonObject;
import org.aksw.jenax.io.json.accumulator.AggJsonProperty;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GraphToJsonNodeMapperObject
    implements GraphToJsonNodeMapper
{
    protected Map<String, GraphToJsonPropertyMapper> propertyMappers = new LinkedHashMap<>();

    public Map<String, GraphToJsonPropertyMapper> getPropertyMappers() {
        return propertyMappers;
    }

    @Override
    public GraphToJsonNodeMapperType getType() {
        return GraphToJsonNodeMapperType.OBJECT;
    }

    @Override
    public JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node) {
        JsonObject result = new JsonObject();
        for (Entry<String, GraphToJsonPropertyMapper> e : getPropertyMappers().entrySet()) {
            String name = e.getKey();
            GraphToJsonPropertyMapper mapper = e.getValue();

            PathJson subPath = path.resolve(Step.of(name));
            JsonElement contrib = mapper.map(subPath, errors, graph, node);

            // TODO If the property mapper's target object has only a single hidden key
            // then replace the object with the value of that key
            // the query foo { bar @hide } with data { foo: { bar: baz } } yields { foo: baz }

            // Flatten json documents of hidden properties
            if (mapper.isHidden()) {
                JsonObject toFlatten = null;
                if (contrib.isJsonArray()) {
                    JsonArray arr = contrib.getAsJsonArray();
                    if (arr.size() == 1) {
                        JsonElement item = arr.get(0);
                        if (item.isJsonObject()) {
                            toFlatten = item.getAsJsonObject();
                        }
                    }
                } else if (contrib.isJsonObject()) {
                    toFlatten = contrib.getAsJsonObject();
                }

                if (toFlatten != null) {
                    for (String key : toFlatten.keySet()) {
                        JsonElement value = toFlatten.get(key);
                        result.add(key, value);
                    }
                }
            } else {
                result.add(name, contrib);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "NodeMapperObject [propertyMappers=" + propertyMappers + "]";
    }

    @Override
    public AggJsonNode toAggregator() {
        AggJsonObject result = AggJsonObject.of();
        propertyMappers.forEach((name, mapper) -> {
            Node node = NodeFactory.createLiteral(name);
            AggJsonProperty agg = mapper.toAggregator(node);
            result.addPropertyAggregator(agg);
        });
        return result;
    }
}