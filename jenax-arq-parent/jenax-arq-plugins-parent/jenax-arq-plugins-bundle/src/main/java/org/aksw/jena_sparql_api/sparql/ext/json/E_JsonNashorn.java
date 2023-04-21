package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class E_JsonNashorn extends FunctionBase {
    protected ScriptEngine engine;
    protected Gson gson;

    protected Object jsonParse;
    protected Object jsonStringify;

    public E_JsonNashorn() throws ScriptException {
        this(
                new ScriptEngineManager().getEngineByName("js"),
                new Gson()
        );
    }


    public E_JsonNashorn(ScriptEngine engine, Gson gson) throws ScriptException {
        this.engine = engine;
        this.gson = gson;

        jsonParse = engine.eval("function(x) { return JSON.parse(x); }");
        jsonStringify = engine.eval("function(x) { return JSON.stringify(x); }");
    }

    public static E_JsonNashorn create() throws ScriptException {
        return new E_JsonNashorn();
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        NodeValue result = null;
        NodeValue fnNv = args.get(0);
        Object fn = null;
        if(fnNv.isString()) {
            String str = fnNv.asUnquotedString();
            try {
                fn = engine.eval(str);
            } catch (ScriptException e) {
                //throw new ExprException(ExceptionUtils.getRootCauseMessage(e));

                throw new QueryParseException(ExceptionUtils.getRootCauseMessage(e),-1,-1);
            }

            List<NodeValue> fnArgs = args.subList(1, args.size());
            List<Object> jsos = new ArrayList<>(fnArgs.size());
            for (NodeValue arg : fnArgs) {
                JsonElement jsonElt = JenaJsonUtils.enforceJsonElement(arg);
                String jsonStr = gson.toJson(jsonElt);
                //Object engineObj = jsonParse.call(null, jsonStr);
                Bindings bindings = engine.createBindings();
                bindings.put("jsonParse", jsonParse);
                bindings.put("arg", jsonStr);
                Object engineObj = null;
                try {
                    engineObj = engine.eval("jsonParse(arg)", bindings);
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
                // JSObject jsObj = (JSObject)engineObj;
                jsos.add(engineObj);
            }

            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

            //Object[] as = jsos.toArray(new Object[0]);
            Bindings bindings = engine.createBindings();
            StringBuilder call = new StringBuilder("__(");
            bindings.put("__", fn);
            for (int i = 0; i < jsos.size(); i++) {
                String var = "_" + i;
                bindings.put(var, jsos.get(i));
                if (i > 0)
                    call.append(',');
                call.append(var);
            }
            call.append(')');
            Object raw = null;
            try {
                raw = engine.eval(call.toString(), bindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            //Object raw = fn.call(fn, as);
            Bindings parseBindings = engine.createBindings();
            parseBindings.put("jsonStringify", jsonStringify);
            parseBindings.put("arg", raw);
            String jsonStr = null;
            try {
                jsonStr = (String) engine.eval("jsonStringify(arg)", parseBindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            //String jsonStr = jsonStringify.call(null, raw).toString();
            JsonElement jsonEl = gson.fromJson(jsonStr, JsonElement.class);
            result = JenaJsonUtils.convertJsonToNodeValue(jsonEl);
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
