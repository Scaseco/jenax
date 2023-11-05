package org.aksw.jenax.arq.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.assembler.VocabGeoSPARQL;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RdfJsonUtils {
    public static JsonElement toJson(RDFNode rdfNode, int maxDepth, boolean flat) {
        JsonElement result = toJson(rdfNode, 0, maxDepth, flat);
        return result;
    }

    public static JsonObject toJson(Resource rdfNode, int maxDepth, boolean flat) {
        JsonElement tmp = toJson(rdfNode, 0, maxDepth, flat);
        JsonObject result = tmp.getAsJsonObject();
        return result;
    }

    public static JsonArray toJson(Collection<? extends RDFNode> rdfNodes, int maxDepth, boolean flat) {
        JsonArray result = new JsonArray();
        for(RDFNode rdfNode : rdfNodes) {
            JsonElement jsonElement = toJson(rdfNode, maxDepth, flat);
            result.add(jsonElement);
        }
        return result;
    }

    public static JsonArray toJson(ResultSet rs, int maxDepth, boolean flat) {
        JsonArray result = new JsonArray();
        List<String> vars = rs.getResultVars();
        while(rs.hasNext()) {
            QuerySolution qs = rs.next();
            JsonObject row = toJson(qs, maxDepth, flat);
            if (flat && vars.size() == 1) {
                result.add(row.entrySet().iterator().next().getValue());
            } else {
                result.add(row);
            }
        }

        return result;
    }

    public static JsonObject toJson(QuerySolution qs, int maxDepth, boolean flat) {
        JsonObject row = new JsonObject();
        Iterator<String> it = qs.varNames();
        while (it.hasNext()) {
            String varName = it.next();
            RDFNode rdfNode = qs.get(varName);
            JsonElement jsonElement = toJson(rdfNode, maxDepth, flat);
            row.add(varName, jsonElement);
        }

        return row;
    }

    public static JsonElement toJson(RDFNode rdfNode, int depth, int maxDepth, boolean flat) {
        Graph g = rdfNode.getModel().getGraph();
        Node node = rdfNode.asNode();
        JsonElement result = toJson(g, node, depth, maxDepth, flat);
        return result;
    }

    public static JsonElement toJson(Graph graph, Node node, int depth, int maxDepth, boolean flat) {
        JsonElement result;

        if(depth >= maxDepth) {
            // TODO We could add properties indicating that data was cut off here
            result = null; // new JsonObject();
        } else if(node == null) {
            result = JsonNull.INSTANCE;
        } else if(node.isLiteral()) {
            Object obj = node.getLiteralValue();
            //boolean isNumber =//NodeMapperRdfDatatype.canMapCore(node, Number.class);
            //if(isNumber) {
            if(obj instanceof String) {
                String value = (String)obj;
                result = new JsonPrimitive(value);
            } else if(obj instanceof Number) {
                Number value = (Number)obj; //NodeMapperRdfDatatype.toJavaCore(node, Number.class);
//				Literal literal = rdfNode.asLiteral();
                result = new JsonPrimitive(value);
            } else if(obj instanceof Boolean) {
                Boolean value = (Boolean) obj;
                result = new JsonPrimitive(value);
            } else if(obj instanceof GeometryWrapper) {
                // TODO Add an extension point for custom datatypes
                GeometryWrapper w = (GeometryWrapper)obj;
                String datatypeUri = node.getLiteralDatatypeURI();
                switch (datatypeUri) {
                case Geo.WKT:
                    result = new JsonPrimitive(WKTWriter.write(w));
                    break;
                case Geo.GEO_JSON:
                    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
                    Geometry geom = w.getParsingGeometry();
                    String jsonString = geoJsonWriter.write(geom);
                    Gson gson = new Gson();
                    result = gson.fromJson(jsonString, JsonObject.class);
                    break;
                default:
                    result = new JsonPrimitive(node.getLiteralLexicalForm());
                    break;
                }
            } else {
                String value = node.getLiteralLexicalForm(); // Objects.toString(obj);
                result = new JsonPrimitive(value ) ; //+ "^^" + obj.getClass().getCanonicalName());
//				throw new RuntimeException("Unsupported literal: " + rdfNode);
            }
        } else if(!node.isLiteral()) { // if node is a resource
            JsonObject tmp = new JsonObject();
            // Resource r = rdfNode.asResource();

            if(node.isURI()) {
                tmp.addProperty("id", node.getURI());
                tmp.addProperty("id_type", "uri");
            } else if(node.isBlank()) {
                tmp.addProperty("id", node.getBlankNodeLabel());
                tmp.addProperty("id_type", "bnode");
            }

            // List<Statement> stmts = r.listProperties().toList();
            List<Triple> stmts = graph.find(node, Node.ANY, Node.ANY).toList();



            Map<Node, List<Node>> pos = stmts.stream()
                    .collect(Collectors.groupingBy(Triple::getPredicate,
                            Collectors.mapping(Triple::getObject, Collectors.toList())));

            for(Entry<Node, List<Node>> e : pos.entrySet()) {
                JsonArray arr = new JsonArray();
                Node p = e.getKey();
                String k = p.getLocalName();

                for(Node o : e.getValue()) {
                    JsonElement v = toJson(graph, o, depth + 1, maxDepth, flat);
                    if (v != null)
                    arr.add(v);
                }

                if (arr.size() > 0) {
                    if (flat && arr.size() == 1)
                        tmp.add(k, arr.get(0));
                    else
                        tmp.add(k, arr);
                }
            }
            result = tmp;
        } else {
            throw new RuntimeException("Unknown node type: " + node);
        }

        return result;
    }
}
