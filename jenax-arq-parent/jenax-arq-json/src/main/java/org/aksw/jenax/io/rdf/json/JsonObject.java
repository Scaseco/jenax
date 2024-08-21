package org.aksw.jenax.io.rdf.json;

import java.util.Map;

import com.google.gson.JsonElement;

public interface JsonObject {
    Map<String, JsonElement> getElements();
}
