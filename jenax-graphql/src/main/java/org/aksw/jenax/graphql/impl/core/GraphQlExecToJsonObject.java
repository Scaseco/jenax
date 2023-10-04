package org.aksw.jenax.graphql.impl.core;

import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.GraphQlExec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GraphQlExecToJsonObject {
    // @Override
    public JsonObject write(GraphQlExec exec) {
        // { data: { ...  dataStreams ... } }
        JsonObject dataObject = new JsonObject();

        Set<String> dataStreamNames = exec.getDataStreamNames();
        for (String name : dataStreamNames) {
            // TODO Handle the case of non-array responses
            JsonArray items = new JsonArray();
            try (Stream<JsonElement> stream = exec.getDataStream(name)) {
                stream.forEach(items::add);
            }
            dataObject.add(name, items);
        }

        JsonArray errorsArray = new JsonArray();

        JsonObject result = new JsonObject();
        result.add("data", dataObject);
        result.add("errors", errorsArray);
        return result;
    }
}
