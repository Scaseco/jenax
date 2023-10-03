package org.aksw.jenax.io.json.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NodeConverterObject
    implements NodeConverter
{
    protected Map<String, PropertyConverter> propertyConverters = new LinkedHashMap<>();

    public Map<String, PropertyConverter> getPropertyConverters() {
        return propertyConverters;
    }

    @Override
    public RdfToJsonConverterType getType() {
        return RdfToJsonConverterType.OBJECT;
    }

    @Override
    public JsonElement convert(Graph graph, Node node) {
        JsonObject result = new JsonObject();
        for (Entry<String, PropertyConverter> e : getPropertyConverters().entrySet()) {
            String name = e.getKey();
            PropertyConverter converter = e.getValue();
            JsonElement contrib = converter.convert(graph, node);

            // Flatten json documents of hidden properties
            if (converter.isHidden()) {
                if (contrib.isJsonObject()) {
                    JsonObject toFlatten = contrib.getAsJsonObject();
                    for (String key : toFlatten.keySet()) {
                        JsonElement value = toFlatten.get(key);
                        result.add(key, value);
                    }
                }
            }

            result.add(name, contrib);
        }
        return result;
    }

    @Override
    public String toString() {
        return "NodeConverterObject [propertyConverters=" + propertyConverters + "]";
    }
}
