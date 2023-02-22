package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.rdf.collections.NodeMapperFromRdfDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

public class JenaJsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JenaJsonUtils.class);

    private static final Cache<String, JsonPath> pathCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private static final ParseContext parseContext = JsonPath.using(Configuration.builder()
            .jsonProvider(new GsonJsonProvider())
            //.options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS)
            .build());


    public static NodeValue fromString(String jsonStr) {
        Node node = NodeFactory.createLiteral(jsonStr, RDFDatatypeJson.get());
        NodeValue result = NodeValue.makeNode(node);
        return result;
    }

//    TODO Needs adaption (copyied from XML)
//    public static NodeValue resolve(NodeValue nv, FunctionEnv env) throws Exception {
//        RDFDatatypeXml dtype = (RDFDatatypeXml)TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);
//        NodeValue result;
//        try (InputStream in = JenaUrlUtils.openInputStream(nv, env)) {
//            if (in != null) {
//                Gson gson = new GsonBuilder().setLenient().create();
//
//                result = JenaXmlUtils.parse(in, dtype);
//            } else {
//                throw new ExprEvalException("Failed to obtain text from node " + nv);
//            }
//        }
//        return result;
//    }

    public static boolean isJsonElement(NodeValue nv) {
        boolean result = false;
        if (nv != null) {
            Node asNode = nv.asNode();
            result = asNode.getLiteralDatatype() instanceof RDFDatatypeJson;
        }
        return result;
    }

    public static JsonElement extractJsonElement(NodeValue nv) {
        JsonElement result = null;
        if (nv != null) {
            Node asNode = nv.asNode();
            if(asNode.getLiteralDatatype() instanceof RDFDatatypeJson) {
                result = (JsonElement)asNode.getLiteralValue();
            }
        }

        return result;
    }

    /** Extract or convert */
    public static JsonElement enforceJsonElement(NodeValue nv) {
        JsonElement result = extractJsonElement(nv);
        if (result == null) {
            Node node = nv.asNode();
            result = nodeToJsonElement(node);
        }
        return result;
    }

//    public static Node createPrimitiveNodeValue(Object o) {
//        RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
//        Node result = NodeFactory.createLiteralByValue(o, dtype);
//        return result;
//    }


    public static JsonElement nodeToJsonElement(Node node) {
        JsonElement result;
        if (node == null) {
            result = JsonNull.INSTANCE;
        } else if (node.isLiteral()) {
            result = nodeLiteralToJsonElement(node);
        } else if (node.isURI()) {
            result = new JsonPrimitive(node.getURI());
        } else if (node.isBlank()) {
            result = new JsonPrimitive(node.getBlankNodeLabel());
        } else {
            throw new IllegalArgumentException("Unknown term type - Neither literal, iri nor blank node: " + node);
        }

        return result;
    }

    public static JsonElement nodeLiteralToJsonElement(Node node) {
        JsonElement result;
        if (!node.isLiteral()) {
            throw new IllegalArgumentException("Not a literal");
        } else {
            Object obj = node.getLiteralValue();
            if(obj instanceof String) {
                String value = (String)obj;
                result = new JsonPrimitive(value);
            } else if(obj instanceof Number) {
                Number value = (Number)obj;
                result = new JsonPrimitive(value);
            } else if(obj instanceof Boolean) {
                Boolean value = (Boolean) obj;
                result = new JsonPrimitive(value);
            } else {
                // Fallback: Just emit the lexical form
                String value = node.getLiteralLexicalForm();
                result = new JsonPrimitive(value);
            }
        }
        return result;
    }

    public static Node jsonToNode(Object o) {
        Gson gson = new Gson();
        RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);
        Node result = jsonToNode(o, gson, dtype);
        return result;
    }
    public static Node jsonToNode(Object o, Gson gson, RDFDatatype jsonDatatype) {
        boolean isPrimitive = o instanceof Boolean || o instanceof Number || o instanceof String;

        Node result;
        if(o == null) {
            result = null;
        } else if(isPrimitive) {
            RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
            result = NodeFactory.createLiteralByValue(o, dtype);
        } else if(o instanceof JsonElement) {
            JsonElement e = (JsonElement)o;
            result = jsonToNode(e, gson, jsonDatatype);
        } else {
            // Write the object to json and re-read it as a json-element
            String str = gson.toJson(o);
            JsonElement e = gson.fromJson(str, JsonElement.class);
            result = jsonToNode(e, gson, jsonDatatype);
        }
//    	else {
//    		throw new RuntimeException("Unknown type: " + o);
//    	}

        return result;
    }

    public static Node jsonToNode(JsonElement e, Gson gson, RDFDatatype jsonDatatype) {
        Node result;
        if(e == null) {
            result = null;
        } else if(e.isJsonPrimitive()) {
            //JsonPrimitive p = e.getAsJsonPrimitive();
            Object o = gson.fromJson(e, Object.class); //JsonTransformerUtils.toJavaObject(p);

            if(o != null) {
                RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
                result = NodeFactory.createLiteralByValue(o, dtype);
            } else {
                throw new RuntimeException("Datatype not supported " + e);
            }
        } else if(e.isJsonObject() || e.isJsonArray()) { // arrays are json objects / array e.isJsonArray() ||
            result = NodeFactory.createLiteralByValue(e, jsonDatatype);//new NodeValueJson(e);
        } else if (e.isJsonNull()) {
            result = null;
        } else {
            throw new RuntimeException("Datatype not supported " + e);
        }

        return result;
    }

    public static NodeValue evalJsonPath(Gson gson, NodeValue nv, NodeValue query) {
        RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

        JsonElement json = JenaJsonUtils.extractJsonElement(nv);

        NodeValue result = null;
        if(query.isString() && json != null) {
            // Object tmp = gson.fromJson(json, Object.class); //JsonTransformerObject.toJava.apply(json);
            String queryStr = query.getString();
            Object o = null;
            try {
                // If parsing the JSON fails, we return nothing, yet we log an error
                JsonPath compiledPath = pathCache.get(queryStr, () -> JsonPath.compile(queryStr));

                // JsonPath compiledPath = JsonPath.compile(queryStr);
                o = parseContext.parse(json).read(compiledPath);
            } catch(Exception e) {
                logger.warn(e.getLocalizedMessage());
                NodeValue.raise(new ExprTypeException("Error evaluating json path", e));
                //result = NodeValue.nvNothing;
            }

            if (o == null) {
                NodeValue.raise(new ExprTypeException("JsonPath evaluated to null"));
            } else {
                Node node = JenaJsonUtils.jsonToNode(o, gson, jsonDatatype);
                if (node == null) {
                    NodeValue.raise(new ExprTypeException("Json evaluated to null (probably JsonNull)"));
                }
                result = NodeValue.makeNode(node);
            }
        } else {
            NodeValue.raise(new ExprTypeException("Invalid arguments to json path"));
        }

        return result;
    }

    public static QueryIterator unnestJsonArray(Gson gson, Binding binding, Node index, ExecutionContext execCxt, Node node, Var outputVar) {
        Var indexVarTmp = null;
        Integer indexVal = null;

        if (index != null) {
            if(index.isVariable()) {
                indexVarTmp = (Var)index;
//                    throw new RuntimeException("Index of json array unnesting must be a variable");
            } else if(index.isLiteral()) {
                Object obj = NodeMapperFromRdfDatatype.toJavaCore(index, index.getLiteralDatatype());
                if(obj instanceof Number) {
                    indexVal = ((Number)obj).intValue();
                } else {
                    throw new ExprEvalException("Index into json array is a literal but not a number: " + index);
                }
            } else {
                throw new ExprEvalException("Index into json array is not a number " + index);
            }
        }
        Var indexVar = indexVarTmp;


        QueryIterator result = null;

        boolean isJson = node != null && node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeJson;
        if(isJson) {
            JsonElement data = (JsonElement)node.getLiteralValue();
            if(data != null && data.isJsonArray()) {
                JsonArray arr = data.getAsJsonArray();

                Iterator<Binding> it;
                if(indexVal != null) {
                    Binding b = itemToBinding(binding, arr, indexVal, gson, indexVar, outputVar);
                    it = Collections.singleton(b).iterator();
                } else {
                    it = IntStream.range(0, arr.size()).mapToObj(i -> {
                        Binding r = itemToBinding(binding, arr, i, gson, indexVar, outputVar);
                        return r;
                    }).iterator();
                }
                result = QueryIterPlainWrapper.create(it, execCxt);
            }
        }

        if(result == null) {
            result = QueryIterNullIterator.create(execCxt);
        }
        return result;
    }

    public static Binding itemToBinding(Binding binding, JsonArray arr, int i, Gson gson, Var indexVar, Var outputVar) {
        JsonElement item;

        try {
            item = arr.get(i);
        } catch(Exception e) {
            throw new ExprEvalException(e);
        }
        RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

        Node n = JenaJsonUtils.jsonToNode(item, gson, jsonDatatype);
        // NodeValue nv = n == null ? null : NodeValue.makeNode(n);

        if (n != null) {
            binding = BindingFactory.binding(binding, outputVar, n);
        }

        if(indexVar != null) {
            binding = BindingFactory.binding(binding, indexVar, NodeValue.makeInteger(i).asNode());
        }

        return binding;
    }
//    public static NodeValue jsonToNodeValue(Object o) {
//    	NodeValue result;
//    	if(o == null) {
//    		result = NodeValue.nvNothing;
//    	} else if(o instanceof Number) {
//        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
//        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
//        	result = NodeValue.makeNode(node);
//        } else if(o instanceof String) {
//        	result = NodeValue.makeString((String)o);
//        } else {
//            result = new NodeValueJson(o);
//        }
//
//        return result;
//    }

}
