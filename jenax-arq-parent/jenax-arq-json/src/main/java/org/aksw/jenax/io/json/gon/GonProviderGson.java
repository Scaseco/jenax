package org.aksw.jenax.io.json.gon;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GonProviderGson
    implements GonProvider<String, JsonPrimitive>
{
    protected Gson gson;

    protected GonProviderGson(Gson gson) {
        super();
        this.gson = gson;
    }

    public static GonProviderGson of() {
        return of(new GsonBuilder().setPrettyPrinting().setLenient().create());
    }

    public static GonProviderGson of(Gson gson) {
        Objects.requireNonNull(gson);
        return new GonProviderGson(gson);
    }

    @Override
    public Object parse(String str) {
        Object result = gson.fromJson(str, JsonElement.class);
        return result;
    }

    @Override
    public Object newObject() {
        return new JsonObject();
    }

    @Override
    public boolean isObject(Object obj) {
        return obj instanceof JsonObject;
    }

    protected static JsonElement asElement(Object obj) {
        JsonElement result = (JsonElement)obj;
        return result;
    }

    protected static JsonObject asObject(Object obj) {
        JsonObject result = asElement(obj).getAsJsonObject();
        return result;
    }

    protected static JsonArray asArray(Object obj) {
        JsonArray result = asElement(obj).getAsJsonArray();
        return result;
    }

    protected static JsonPrimitive asPrimitive(Object obj) {
        JsonPrimitive result = asElement(obj).getAsJsonPrimitive();
        return result;
    }

    @Override
    public void setProperty(Object obj, String key, Object value) {
        asObject(obj).add(key, asElement(value));
    }

    @Override
    public Object getProperty(Object obj, Object key) {
        return asObject(obj).get((String)key);
    }

    @Override
    public void removeProperty(Object obj, Object key) {
        asObject(obj).remove((String)key);
    }

    @Override
    public Iterator<Entry<String, Object>> listProperties(Object obj) {
        return (Iterator)asObject(obj).entrySet().iterator();
    }

    @Override
    public Object newArray() {
        return new JsonArray();
    }

    @Override
    public boolean isArray(Object obj) {
        return obj instanceof JsonArray;
    }

    @Override
    public void addElement(Object arr, Object value) {
        asArray(arr).add(asElement(value));
    }

    @Override
    public void setElement(Object arr, int index, Object value) {
        asArray(arr).set(index, asElement(value));
    }

    @Override
    public void removeElement(Object arr, int index) {
        asArray(arr).remove(index);
    }

    @Override
    public Iterator<Object> listElements(Object arr) {
        return (Iterator)asArray(arr).iterator();
    }

    @Override
    public Object newLiteral(JsonPrimitive value) {
        return value;
    }

    @Override
    public boolean isLiteral(Object obj) {
        return obj instanceof JsonPrimitive;
    }

    @Override
    public JsonPrimitive getLiteral(Object obj) {
        return asPrimitive(obj);
    }

    @Override
    public Object newNull() {
        return JsonNull.INSTANCE;
    }

    @Override
    public boolean isNull(Object obj) {
        return asElement(obj).isJsonNull();
    }
}
