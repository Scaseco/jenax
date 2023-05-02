package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class E_JsonNashorn extends FunctionBase {
    protected ScriptEngine engine;
    protected Gson gson;

    protected Object jsonParse;
    protected Object jsonStringify;
    protected boolean implicit;

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

    public static Object call(ScriptEngine engine, Object fn, Iterable<Object> args) {
        Bindings bindings = engine.createBindings();
        StringBuilder call = new StringBuilder("__(");
        bindings.put("__", fn);
        int i = 0;
        for (Object a : args) {
            String var = "_" + i;
            bindings.put(var, a);
            if (i > 0)
                call.append(',');
            call.append(var);
            i++;
        }
        call.append(')');
        try {
            return engine.eval(call.toString(), bindings);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object call(ScriptEngine engine, Object fn, Object... args) {
        return call(engine, fn, Arrays.asList(args));
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        NodeValue result = null;
        NodeValue fnNv = args.get(0);
        Object fn = null;
        if(fnNv.isString()) {
            String str = fnNv.asUnquotedString();
            try {
                fn = engine.eval(functionString(str, args.size() - 1));
            } catch (ScriptException e) {
                //throw new ExprException(ExceptionUtils.getRootCauseMessage(e));

                throw new QueryParseException(ExceptionUtils.getRootCauseMessage(e),-1,-1);
            }

            List<NodeValue> fnArgs = args.subList(1, args.size());
            List<Object> jsos = new ArrayList<>(fnArgs.size());
            for (NodeValue arg : fnArgs) {
                JsonElement jsonElt = JenaJsonUtils.enforceJsonElement(arg);
                String jsonStr = gson.toJson(jsonElt);
                Object engineObj = call(engine, jsonParse, jsonStr);
                jsos.add(engineObj);
            }

            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

            Object raw = call(engine, fn, jsos);

            String jsonStr = (String) call(engine, jsonStringify, raw);;
            JsonElement jsonEl = gson.fromJson(jsonStr, JsonElement.class);
            result = JenaJsonUtils.convertJsonToNodeValue(jsonEl);
            return result;
        }
        throw new ExprEvalException("json:js: Weird function argument (arg 1): "+fnNv);
    }

    protected String functionString(String str, int argc) {
        if (this.implicit) {
            StringBuilder s = new StringBuilder("function (");
            for (int i = 0; i < argc; i++) {
                if (i > 0)
                    s.append(',');
                s.append("$").append(i);
            }
            s.append(") {");
            s.append(str);
            s.append("}");
            return s.toString();
        }
        return str;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if(args.size() < 1) {
            throw new RuntimeException("At least 1 argument required for JavaScript call");
        }
        this.implicit = uri.endsWith("e");
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
