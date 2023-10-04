package org.aksw.jenax.graphql.impl.sparql;

import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.path.json.PathJson;
import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.io.json.mapper.RdfToJsonMapper;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.ModelUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import io.reactivex.rxjava3.core.Flowable;

public class GraphQlExecImpl
    implements GraphQlExec
{
    protected RdfDataSource dataSource;
    protected GraphQlToSparqlMapping mapping;

    public GraphQlExecImpl(RdfDataSource dataSource, GraphQlToSparqlMapping mapping) {
        super();
        this.dataSource = dataSource;
        this.mapping = mapping;
    }

    @Override
    public Set<String> getDataStreamNames() {
        return mapping.getTopLevelMappings().keySet();
    }

    @Override
    public Stream<JsonElement> getDataStream(String name) {
        GraphQlToSparqlMapping.Entry entry = mapping.getTopLevelMappings().get(name);

        NodeQuery nodeQuery = entry.getNodeQuery();
        RdfToJsonMapper jsonMapper = entry.getMapper();
        RelationQuery rq = nodeQuery.relationQuery();
        Query query = ElementGeneratorLateral.toQuery(rq);

        FlowableOperatorSequentialGroupBy<Quad, Node, Graph> grouper = FlowableOperatorSequentialGroupBy.create(
                SequentialGroupBySpec.create(
                    Quad::getGraph,
                    graphNode -> GraphFactoryEx.createInsertOrderPreservingGraph(), // GraphFactory.createDefaultGraph(),
                    (graph, quad) -> graph.add(quad.asTriple())));

        Flowable<RDFNode> graphFlow = SparqlRx
                .execConstructQuads(() -> dataSource.asQef().createQueryExecution(query))
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
    }
}
