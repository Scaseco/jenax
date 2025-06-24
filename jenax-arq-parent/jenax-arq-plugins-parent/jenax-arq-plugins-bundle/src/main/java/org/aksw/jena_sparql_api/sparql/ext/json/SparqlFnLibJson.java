package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.commons.util.gson.GsonUtils;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.norse.term.json.NorseTermsJson;
import org.apache.jena.graph.Node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SparqlFnLibJson {

    /** Get the length of a json array. Raises {@link IllegalArgumentException} for non array arguments */
    @IriNs(JenaExtensionJson.LEGACY_NS)
    public static int length(JsonElement json) {
        int result;
        if (json.isJsonArray()) {
            result = json.getAsJsonArray().size();
        } else {
            throw new IllegalArgumentException("Argument is not a json array");
        }

        return result;
    }

    /**
     * Creates a deep copy of the given json object with the given key and
     * value (converted to JSON) appended. Overwrites any prior value for that key.
     */
    @IriNs(JenaExtensionJson.LEGACY_NS)
    public static JsonElement set(JsonElement json, String key, Node value) {
        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("json:set() requires a json object as first argument");
        }
        JsonObject result = json.getAsJsonObject().deepCopy();
        JsonElement elt = JenaJsonUtils.nodeToJsonElement(value);
        result.add(key, elt);
        return result;
    }

    @IriNs(NorseTermsJson.merge)
    public static JsonElement merge(JsonElement a, JsonElement b) {
        JsonElement result = GsonUtils.merge(GsonUtils.copyOrNull(a), b);
        return result;
    }
}
