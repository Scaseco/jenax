package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
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
        JsonElement elt = JenaJsonUtils.requireJsonElement(nv);
        NodeValue result = null;
        if (key.isInteger()) {
            int idx = key.getInteger().intValue();
            if (elt.isJsonArray()) {
                JsonArray arr = elt.getAsJsonArray();
                JsonElement item = arr.get(idx);
                result = JenaJsonUtils.convertJsonToNodeValue(item);
            } else {
                NodeValue.raise(new ExprEvalTypeException("Integer key type can only be used to access JSON array elements"));
            }
        } else if (key.isString()) {
            String str = key.getString();
            if (elt.isJsonObject()) {
                JsonElement val = elt.getAsJsonObject().get(str);
                result = JenaJsonUtils.convertJsonOrValueToNodeValue(val);
            } else {
                NodeValue.raise(new ExprEvalTypeException("String key type can only be used to access members of JSON objects"));
            }
        } else {
            NodeValue.raise(new ExprEvalTypeException("Json array or object expected"));
        }

        if (result == null) {
            NodeValue.raise(new ExprEvalException("Access of JSON object by key returned null value"));
        }

        return result;
    }
}
