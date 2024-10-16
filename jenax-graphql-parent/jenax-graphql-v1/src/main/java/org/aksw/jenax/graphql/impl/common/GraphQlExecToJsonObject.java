package org.aksw.jenax.graphql.impl.common;

import java.util.stream.Stream;

import org.aksw.jenax.graphql.json.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.json.api.GraphQlExec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GraphQlExecToJsonObject {
    // @Override
    public JsonObject write(GraphQlExec exec) {
        // { data: { ...  dataStreams ... } }
        JsonObject dataObject = new JsonObject();

        for (GraphQlDataProvider dataProvider : exec.getDataProviders()) {
            String name = dataProvider.getName();

            // TODO Handle the case of non-array responses
            JsonArray items = new JsonArray();
            try (Stream<JsonElement> stream = dataProvider.openStream()) {
                stream.forEach(items::add);
            }
            dataObject.add(name, items);
        }

        JsonArray errorsArray = new JsonArray();

        JsonObject result = new JsonObject();
        result.add("data", dataObject);
        result.add("errors", errorsArray);

        JsonObject metadata = GraphQlExecUtils.collectExtensions(exec);
        if (!metadata.keySet().isEmpty()) {
            result.add("extensions", metadata);
        }

        return result;
    }
}
