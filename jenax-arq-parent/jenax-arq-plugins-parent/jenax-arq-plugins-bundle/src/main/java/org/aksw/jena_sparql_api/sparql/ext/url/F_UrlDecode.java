package org.aksw.jena_sparql_api.sparql.ext.url;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class F_UrlDecode extends FunctionBase1 {
    public static final String tagUrlDecode = "urlDecode";

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        if (nv != null) {
            if (nv.isString()) {
                String encoded = nv.getString();
                try {
                    String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
                    result = NodeValue.makeNode(NodeFactory.createLiteralString(decoded));
                } catch (Exception e) {
                    throw new ExprEvalException(tagUrlDecode + ": Failed to decode " + encoded, e);
                }
            } else {
                throw new ExprEvalException(tagUrlDecode + ": Argument is not a string");
            }
        } else {
            throw new ExprEvalException(tagUrlDecode + ": Called with null / unbound");
        }
        return result;
    }
}
