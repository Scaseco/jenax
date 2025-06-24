package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecSelect;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlDataProvider;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlDataProviderBase;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;
import org.aksw.jenax.io.json.accumulator.AccContextRdf;
import org.aksw.jenax.io.json.accumulator.AccJson;
import org.aksw.jenax.io.json.accumulator.AccJsonDriver;
import org.aksw.jenax.io.json.accumulator.AggJson;
import org.aksw.jenax.io.json.graph.GraphToJsonMapperNode;
import org.aksw.jenax.io.json.writer.RdfObjectNotationWriter;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExecAdapter;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

import com.google.gson.JsonObject;

import graphql.language.Field;

public class RdfGraphQlExecImpl
    implements RdfGraphQlExec
{
    protected RDFDataSource dataSource;
    protected GraphQlToSparqlMapping mapping;
    protected List<RdfGraphQlDataProvider> dataProviders;

    public RdfGraphQlExecImpl(RDFDataSource dataSource, GraphQlToSparqlMapping mapping) {
        super();
        this.dataSource = dataSource;
        this.mapping = mapping;
    }

    public RDFDataSource getDataSource() {
        return dataSource;
    }

    public GraphQlToSparqlMapping getMapping() {
        return mapping;
    }

    @Override
    public Set<String> getDataProviderNames() {
        return mapping.getTopLevelMappings().keySet();
    }

    @Override
    public RdfGraphQlDataProvider getDataProvider(String name) {
        GraphQlToSparqlMapping.Entry entry = mapping.getTopLevelMappings().get(name);
        boolean isSingle = entry.isSingle();

        // GraphQlToSparqlConverter.setupContext(null)
        // entry.getTopLevelField();

        PrefixMap prefixMap = entry.getPrefixMap();

        NodeQuery nodeQuery = entry.getNodeQuery();
        GraphToJsonMapperNode jsonMapper = entry.getMapper();

        RelationQuery rq = nodeQuery.relationQuery();
        Query query = ElementGeneratorLateral.toQuery(rq);
        query.setPrefixMapping(new PrefixMappingAdapter(prefixMap));

        Field field = entry.getTopLevelField();
        JsonObject metadata = null;
        if (field.hasDirective("debug")) {
            metadata = new JsonObject();
            metadata.addProperty("sparqlQuery", query.toString());
            // metadata.addProperty("isSingle", entry.isSingle());
        }

        // System.err.println(query.toString());

        RdfGraphQlDataProvider result;

        // true = fully streaming approach ; false = legacy
        boolean useAccumulators = true;

        if (useAccumulators) {
            AggJson agg = jsonMapper.toAggregator();
            Supplier<Stream<Quad>> quadStreamSupplier = () -> SparqlRx
                    .execConstructQuads(() ->
                        // Produce quads by executing the construct query as a select one
                        QueryExecutionAdapter.adapt(
                            QueryExecSelect.of(
                                query,
                                q -> QueryExecAdapter.adapt(dataSource.asQef().createQueryExecution(q)),
                                true // raw tuples
                                )))
                    .blockingStream();

            result = new RdfGraphQlDataProviderBase(name, metadata) {
                @Override
                public Stream<RdfElement> openStream() {
                    AccJson acc = agg.newAccumulator();
                    AccJsonDriver driver = AccJsonDriver.of(acc, isSingle);
                    AccContextRdf context = AccContextRdf.materializing();
                    // RdfElementVisitorRdfToJson converter = new RdfElementVisitorRdfToJson();
                    Stream<Quad> quadStream = quadStreamSupplier.get();
                    Stream<RdfElement> r = driver.asStream(context, quadStream)
                        .map(Entry::getValue);
                    return r;

//                    Stream<JsonElement> r = driver.asStream(context, quadStream)
//                            .map(Entry::getValue)
//                            .map(elt -> elt.accept(converter));
//                    return r;
                }

                @Override
                public Query getQuery() {
                    return query;
                }

                @Override
                public void write(RdfObjectNotationWriter writer) throws IOException {
                    AccJson acc = agg.newAccumulator();
                    AccJsonDriver driver = AccJsonDriver.of(acc, isSingle);
                    AccContextRdf context = AccContextRdf.serializing(writer);

                    quadStreamSupplier.get().forEach(quad -> {
                        try {
                            driver.accumulate(quad, context);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    driver.end(context);

                    // Write null if in single mode and there were no sources
                    if (isSingle && driver.getSourcesSeen() == 0) {
                        writer.nullValue();
                    }
                }

                @Override
                public boolean isSingle() {
                    return isSingle;
                }
            };

        } else {

            throw new UnsupportedOperationException("legacy implementation no longer supported");
//            result = new GraphQlDataProviderBase(name, metadata) {
//                @Override
//                public Stream<JsonElement> openStream() {
//                    FlowableOperatorCollapseRuns<Quad, Node, Graph> grouper = FlowableOperatorCollapseRuns.create(
//                        CollapseRunsSpec.create(
//                            Quad::getGraph,
//                            graphNode -> GraphFactoryEx.createInsertOrderPreservingGraph(), // GraphFactory.createDefaultGraph(),
//                            (graph, quad) -> graph.add(quad.asTriple())));
//
//                    Flowable<RDFNode> graphFlow = SparqlRx
//                        .execConstructQuads(() ->
//                            // Produce quads by executing the construct query as a select one
//                            QueryExecutionAdapter.adapt(
//                                QueryExecSelect.of(
//                                    query, q ->
//                                    QueryExecAdapter.adapt(dataSource.asQef().createQueryExecution(q)))))
//                        .lift(grouper)
//                        .map(e -> ModelUtils.convertGraphNodeToRDFNode(e.getKey(), ModelFactory.createModelForGraph(e.getValue())));
//
//                    Flowable<JsonElement> r = graphFlow.map(rdfNode -> {
//                        org.apache.jena.graph.Node node = rdfNode.asNode();
//                        Graph graph = rdfNode.getModel().getGraph();
//                        JsonArray errors = new JsonArray();
//                        JsonElement json = jsonMapper.map(PathJson.newAbsolutePath(), errors, graph, node);
//                        // TODO Handle errors
//                        return json;
//                    });
//                    return r.blockingStream();
//                }
//
//                @Override
//                public void write(JsonWriter writer, Gson gson) {
//                  try (Stream<JsonElement> stream = openStream()) {
//                      stream.forEach(item -> gson.toJson(item, writer));
//                  }
//                }
//
//                @Override
//                public boolean isSingle() {
//                    return isSingle;
//                }
//            };
        }

        return result;
    }
}
