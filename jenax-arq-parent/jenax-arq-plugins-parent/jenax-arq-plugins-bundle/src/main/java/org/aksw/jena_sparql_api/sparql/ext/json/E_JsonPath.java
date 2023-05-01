package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

import com.google.gson.Gson;

/**
 * jsonLiteral json:path(jsonLiteral, queryString)
 *
 * @author raven
 *
 */
public class E_JsonPath
    extends FunctionBase2
{
    private Gson gson;

    public E_JsonPath() {
        this(RDFDatatypeJson.get().getGson());
    }

    public E_JsonPath(Gson gson) {
        super();
        this.gson = gson;
    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {
        return JenaJsonUtils.evalJsonPath(gson, nv, query);
    }
}
