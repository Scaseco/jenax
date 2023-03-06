package org.aksw.jena_sparql_api.sparql.ext.url;

import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.json.JenaJsonUtils;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import com.google.gson.JsonObject;

public class E_UrlFetchSpec
    extends FunctionBase
{
    @Override
    public NodeValue exec(List<NodeValue> args) {
        JsonObject obj = E_UrlFetch.assemble(args);
        // TODO Also try to map the json to the internal java class?
        // UrlFetchSpec conf = RDFDatatypeJson.get().getGson().fromJson(obj, UrlFetchSpec.class);
        return JenaJsonUtils.makeNodeValue(obj);
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
    }
}