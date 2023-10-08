package org.aksw.jenax.graphql.impl.core;

import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlStream;

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
            GraphQlStream dataStream = exec.getDataStream(name);

            // TODO Handle the case of non-array responses
            JsonArray items = new JsonArray();
            try (Stream<JsonElement> stream = dataStream.openStream()) {
                stream.forEach(items::add);
            }
            dataObject.add(name, items);
        }

        JsonArray errorsArray = new JsonArray();

        JsonObject result = new JsonObject();
        result.add("data", dataObject);
        result.add("errors", errorsArray);

        JsonObject metadata = GraphQlExecUtils.collectMetadata(exec);
        if (!metadata.keySet().isEmpty()) {
            result.add("extensions", metadata);
        }

        return result;
    }
}
