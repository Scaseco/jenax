package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.JsonPath;

/**
 * jsonLiteral jsonp(jsonLiteral, queryString)
 *
 * @author raven
 *
 */
public class E_JsonPath
    extends FunctionBase2
{

    private static final Logger logger = LoggerFactory.getLogger(E_JsonPath.class);

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
        RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

        JsonElement json = JenaJsonUtils.extractJsonElement(nv);

        NodeValue result;
        if(query.isString() && json != null) {
            Object tmp = gson.fromJson(json, Object.class); //JsonTransformerObject.toJava.apply(json);
            String queryStr = query.getString();

            try {
                // If parsing the JSON fails, we return nothing, yet we log an error
                Object o = JsonPath.read(tmp, queryStr);

                Node node = JenaJsonUtils.jsonToNode(o, gson, jsonDatatype);
                result = NodeValue.makeNode(node);
            } catch(Exception e) {
                logger.warn(e.getLocalizedMessage());
                NodeValue.raise(new ExprTypeException("Error evaluating json path", e));
                result = null;
                //result = NodeValue.nvNothing;
            }

        } else {
            NodeValue.raise(new ExprTypeException("Invalid arguments to json path"));
            result = null; //NodeValue.nvNothing;
        }

        return result;
    }
}