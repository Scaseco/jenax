package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class JenaJsonUtils {

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
