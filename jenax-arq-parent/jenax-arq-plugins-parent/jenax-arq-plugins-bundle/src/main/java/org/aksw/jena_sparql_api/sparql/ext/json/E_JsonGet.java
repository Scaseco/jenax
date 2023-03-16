package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Access a JSON objects immediate attribute.
 * Experiment for whether this is significantly faster than evaluating json path expressions.
 *
 * jsonLiteral json:get(jsonLiteral, key)
 *
 * @author raven
 *
 */
public class E_JsonGet
    extends FunctionBase2
{
    public E_JsonGet() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue key) {
        JsonElement elt = JenaJsonUtils.extractJsonElementOrNull(nv);
        NodeValue result = null;
        if (key.isInteger()) {
            if (elt.isJsonArray()) {
                int idx = key.getInteger().intValue();
                JsonArray arr = elt.getAsJsonArray();
                JsonElement item = arr.get(idx);
                result = JenaJsonUtils.convertJsonToNodeValue(item);
            } else {
                NodeValue.raise(new ExprTypeException("Integer key type can only be used to access JSON array elements"));
            }
        } else if (key.isString()) {
            if (elt.isJsonObject()) {
                String str = key.getString();
                JsonElement val = elt.getAsJsonObject().get(str);
                result = JenaJsonUtils.convertJsonOrValueToNodeValue(val);
            } else {
                NodeValue.raise(new ExprTypeException("String key type can only be used to access members of JSON objects"));
            }
        } else {
            NodeValue.raise(new ExprTypeException("Json array or object expected"));
        }
        return result;
    }
}
