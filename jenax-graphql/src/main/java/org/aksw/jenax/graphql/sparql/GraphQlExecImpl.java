package org.aksw.jenax.graphql.sparql;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecSelect;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.graphql.api.GraphQlDataProvider;
import org.aksw.jenax.graphql.api.GraphQlExec;
import org.aksw.jenax.graphql.impl.common.GraphQlDataProviderImpl;
import org.aksw.jenax.io.json.graph.GraphToJsonMapper;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExecAdapter;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.util.ModelUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import graphql.language.Field;
import io.reactivex.rxjava3.core.Flowable;

public class GraphQlExecImpl
    implements GraphQlExec
{
    protected RdfDataSource dataSource;
    protected GraphQlToSparqlMapping mapping;
    protected List<GraphQlDataProvider> dataProviders;

    public GraphQlExecImpl(RdfDataSource dataSource, GraphQlToSparqlMapping mapping) {
        super();
        this.dataSource = dataSource;
        this.mapping = mapping;
    }

    public RdfDataSource getDataSource() {
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
    public GraphQlDataProvider getDataProvider(String name) {
        GraphQlToSparqlMapping.Entry entry = mapping.getTopLevelMappings().get(name);

        // GraphQlToSparqlConverter.setupContext(null)
        // entry.getTopLevelField();

        PrefixMap prefixMap = entry.getPrefixMap();

        NodeQuery nodeQuery = entry.getNodeQuery();
        GraphToJsonMapper jsonMapper = entry.getMapper();
        RelationQuery rq = nodeQuery.relationQuery();
        Query query = ElementGeneratorLateral.toQuery(rq);
        query.setPrefixMapping(new PrefixMappingAdapter(prefixMap));

        Supplier<Stream<JsonElement>> streamFactory = () -> {
            FlowableOperatorSequentialGroupBy<Quad, Node, Graph> grouper = FlowableOperatorSequentialGroupBy.create(
                SequentialGroupBySpec.create(
                    Quad::getGraph,
                    graphNode -> GraphFactoryEx.createInsertOrderPreservingGraph(), // GraphFactory.createDefaultGraph(),
                    (graph, quad) -> graph.add(quad.asTriple())));

            Flowable<RDFNode> graphFlow = SparqlRx
                .execConstructQuads(() ->
                    // Produce quads by executing the construct query as a select one
                    QueryExecutionAdapter.adapt(
                        QueryExecSelect.of(
                            query, q ->
                            QueryExecAdapter.adapt(dataSource.asQef().createQueryExecution(q)))))
                .lift(grouper)
                .map(e -> ModelUtils.convertGraphNodeToRDFNode(e.getKey(), ModelFactory.createModelForGraph(e.getValue())));

            Flowable<JsonElement> result = graphFlow.map(rdfNode -> {
                org.apache.jena.graph.Node node = rdfNode.asNode();
                Graph graph = rdfNode.getModel().getGraph();
                JsonArray errors = new JsonArray();
                JsonElement json = jsonMapper.map(PathJson.newAbsolutePath(), errors, graph, node);
                // TODO Handle errors
                return json;
            });
            return result.blockingStream();
        };

        Field field = entry.getTopLevelField();
        JsonObject metadata = null;
        if (field.hasDirective("debug")) {
            metadata = new JsonObject();
            metadata.addProperty("sparqlQuery", query.toString());
        }

        return new GraphQlDataProviderImpl(name, metadata, streamFactory);
    }
}
