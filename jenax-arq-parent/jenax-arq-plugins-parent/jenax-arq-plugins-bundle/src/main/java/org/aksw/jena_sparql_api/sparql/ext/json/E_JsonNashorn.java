package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class E_JsonNashorn extends FunctionBase {
    protected ScriptEngine engine;
    protected Gson gson;

    protected JSObject jsonParse;
    protected JSObject jsonStringify;

    public E_JsonNashorn() throws ScriptException {
        this(
                new NashornScriptEngineFactory().getScriptEngine("--language=es6"),
                new Gson()
        );
    }


    public E_JsonNashorn(ScriptEngine engine, Gson gson) throws ScriptException {
        this.engine = engine;
        this.gson = gson;


        jsonParse = (JSObject) engine.eval("function(x) { return JSON.parse(x); }");
        jsonStringify = (JSObject) engine.eval("function(x) { return JSON.stringify(x); }");
    }

    public static E_JsonNashorn create() throws ScriptException {
        return new E_JsonNashorn();
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        NodeValue result = null;
        NodeValue fnNv = args.get(0);
        JSObject fn = null;
        if(fnNv.isString()) {
            String str = fnNv.asUnquotedString();
            try {
                fn = (JSObject)engine.eval(str);
            } catch (ScriptException e) {
                //throw new ExprException(ExceptionUtils.getRootCauseMessage(e));

                throw new QueryParseException(ExceptionUtils.getRootCauseMessage(e),-1,-1);
            }

            List<NodeValue> fnArgs = args.subList(1, args.size());
            List<Object> jsos = new ArrayList<>(fnArgs.size());
            for (NodeValue arg : fnArgs) {
            	JsonElement jsonElt = JenaJsonUtils.enforceJsonElement(arg);
            	String jsonStr = gson.toJson(jsonElt);
            	Object engineObj = jsonParse.call(null, jsonStr);
            	// JSObject jsObj = (JSObject)engineObj;
            	jsos.add(engineObj);
            }

            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

            Object[] as = jsos.toArray(new Object[0]);
            Object raw = fn.call(fn, as);
            String jsonStr = jsonStringify.call(null, raw).toString();
            JsonElement jsonEl = gson.fromJson(jsonStr, JsonElement.class);
            Node node = JenaJsonUtils.convertJsonToNode(jsonEl, gson, dtype);
            result = NodeValue.makeNode(node);
        }
        return result;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if(args.size() < 1) {
            throw new RuntimeException("At least 1 argument required for JavaScript call");
        }
    }

//    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
//
//        Gson gson = new Gson();
//
//        Object x = jsonParse.call(null, "3");
//
//        // define an anoymous function
//        JSObject multiply = (JSObject) e.eval("function(x, y) { return x*y; }");
//
//        String resultStr = jsonStringify.call(null, x).toString();
//        System.out.println("STR: " + resultStr);
//
//        // call that anon function
//        System.out.println(multiply.call(null, x, 5));
//
//        // define another anon function
//        JSObject greet = (JSObject) e.eval("function(n) { print('Hello ' + n)}");
//        greet.call(null, "nashorn");
//    }

}
