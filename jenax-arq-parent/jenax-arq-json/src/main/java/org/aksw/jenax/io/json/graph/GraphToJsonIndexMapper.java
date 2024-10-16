package org.aksw.jenax.io.json.graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.commons.path.json.PathJson;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.io.json.accumulator.AggJsonNode;
import org.aksw.jenax.io.json.writer.RdfObjectNotationWriterViaJson;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GraphToJsonIndexMapper
    implements GraphToJsonMapperNode
{
    // protected boolean isForward; // Could in principle be pushed into the fragment
    // TODO We still can't create a fragment where variables are mapped to constants
    //    fragment.getConstantValue(variable) returns non null if every binding of this fragment will bind the variable to the given constant value
    protected Fragment fragment;
    protected Var sourceVar;
    protected Var indexVar;
    // protected P_Path0
    protected Map<P_Path0, Var> fieldToVar = new LinkedHashMap<>();
    protected Map<Var, GraphToJsonMapperNode> subMappers = new LinkedHashMap<>();

    //
    // { "s1": { "p1: [], "p2": [] } }
    //
    //

    // Only valid if there is at most one subMapper
    protected boolean skipIntermediateObject;

    public GraphToJsonIndexMapper(Fragment fragment, Var sourceVar, Var indexVar) {
        super();
        this.fragment = fragment;
        this.sourceVar = sourceVar;
        this.indexVar = indexVar;
    }

    public void addMapping(Var var, P_Path0 path, GraphToJsonMapperNode subMapper) {
        // TODO Check consistency
        fieldToVar.put(path, var);
        subMappers.put(var, subMapper);
    }

    @Override
    public JsonElement map(PathJson path, JsonArray errors, Graph graph, Node node) {
        Objects.requireNonNull(node);
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        JsonObject result = new JsonObject();
        Query query = fragment.toQuery(); // Perhaps inject "ORDER BY (?indexVar)"
        QueryUtils.injectFilter(query, sourceVar, node);
        try (QueryExec qe = QueryExec.dataset(dsg).query(query).build()) {
            RowSet rs = qe.select();
            while (rs.hasNext()) {
                Binding b = rs.next();
                Node indexValue = b.get(indexVar);
                P_Path0 indexKey = new P_Link(indexValue);

                String indexName = RdfObjectNotationWriterViaJson.nodeToJsonKey(indexKey);

                JsonArray arr;
                JsonElement arrElt = result.get(indexName);
                if (arrElt == null) {
                    arr = new JsonArray();
                    result.add(indexName, arr);
                } else {
                    arr = arrElt.getAsJsonArray();
                }

                // Build the object from the mapped fields
                // if (skipIntermediateObject) {
                JsonObject item = new JsonObject();
                for (Entry<P_Path0, Var> e : fieldToVar.entrySet()) {
                    P_Path0 key = e.getKey();
                    String name = RdfObjectNotationWriterViaJson.nodeToJsonKey(key);
                    PathJson subPath = path.resolve(PathJson.Step.of(name));
                    Var v = e.getValue();
                    Node subNode = b.get(v);
                    GraphToJsonMapperNode subMapper = subMappers.get(v);
                    JsonElement subElt = subMapper.map(subPath, errors, graph, subNode);

                    // TODO Make configurable whether to collect values in an array on only accept first match
                    // TODO There should be a predicate for deciding cardinality on a per-property basis

                    item.add(name, subElt);
                }
                arr.add(item);
            }
        }
        return result;
    }

    @Override
    public GraphToJsonNodeMapperType getType() {
        return GraphToJsonNodeMapperType.OBJECT;
    }

    @Override
    public AggJsonNode toAggregator() {



        throw new UnsupportedOperationException();
    }


    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT ?s ?p ?o ?x { ?s ?p ?o BIND(CONCAT('foo-', STR(?p)) AS ?x) }");
        Fragment fragment = FragmentUtils.fromQuery(query);

        Graph graph = RDFParser.create()
            .fromString("""
                PREFIX : <http://www.example.org/>

                :s1 :p1 :o1 .
                :s1 :p1 :o2 .
                :s1 :p2 :o3 .

                :s2 :p3 :o4 .
            """)
            .lang(Lang.TURTLE)
            .toGraph();

        GraphToJsonIndexMapper mapper = new GraphToJsonIndexMapper(fragment, Vars.s, Vars.p);
        mapper.addMapping(Vars.o, new P_Link(NodeFactory.createLiteralString("myProperty")), GraphToJsonNodeMapperLiteral.get());
        mapper.addMapping(Vars.x, new P_Link(NodeFactory.createLiteralString("dummy")), GraphToJsonNodeMapperLiteral.get());

        JsonElement elt = mapper.map(PathJson.newAbsolutePath(), new JsonArray(), graph, NodeFactory.createURI("http://www.example.org/s1"));
        String str = new GsonBuilder().setPrettyPrinting().create().toJson(elt);
        System.out.println(str);
    }

}
