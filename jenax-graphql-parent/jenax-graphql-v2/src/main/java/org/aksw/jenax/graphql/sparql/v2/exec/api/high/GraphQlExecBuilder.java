package org.aksw.jenax.graphql.sparql.v2.exec.api.high;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlFieldExec;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlProcessor;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlProcessorSettings;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.RdfGraphQlProcessorFactoryImpl;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.path.P_Path0;

import graphql.language.Document;
import graphql.language.FragmentDefinition;

/**
 * Builds GraphQl execution objects from a document and variables.
 * The document can be interpreted as JSON or RON.
 */
public class GraphQlExecBuilder
    implements GraphQlProcessorSettings<GraphQlExecBuilder>
{
    protected Creator<QueryExecBuilder> queryExecBuilderFactory;

    protected Document document;
    protected Map<String, Node> assignments = new LinkedHashMap<>(); // Binding?
    protected SchemaNavigator schemaNavigator = null;
    protected Map<String, FragmentDefinition> nameToFragment = new LinkedHashMap<>();

    public GraphQlExecBuilder(Creator<QueryExecBuilder> queryExecBuilderFactory) {
        super();
        this.queryExecBuilderFactory = Objects.requireNonNull(queryExecBuilderFactory);
    }

    @Override
    public GraphQlExecBuilder document(Document document) {
        this.document = document;
        return this;
    }

    @Override
    public GraphQlExecBuilder setVar(String name, Node value) {
        this.assignments.put(name, value);
        return this;
    }

    @Override
    public GraphQlExecBuilder schemaNavigator(SchemaNavigator schemaNavigator) {
        this.schemaNavigator = schemaNavigator;
        return this;
    }

    @Override
    public GraphQlExecBuilder nameToFragment(Map<String, FragmentDefinition> nameToFragment) {
        this.nameToFragment = nameToFragment;
        return this;
    }

    public GraphQlExec<String> buildForJson() {
        GraphQlProcessor<String> processor = RdfGraphQlProcessorFactoryImpl.forJson()
            .newBuilder()
            .document(document)
            .setVars(assignments)
            .schemaNavigator(schemaNavigator)
            .build();
        GraphQlFieldExec<String> exec = processor.newExecBuilder().service(queryExecBuilderFactory).build();
        return new GraphQlExec<>(exec);
    }

    public GraphQlExec<P_Path0> buildForRon() {
        GraphQlProcessor<P_Path0> processor = RdfGraphQlProcessorFactoryImpl.forRon()
            .newBuilder()
            .document(document)
            .setVars(assignments)
            .schemaNavigator(schemaNavigator)
            .build();
        GraphQlFieldExec<P_Path0> exec = processor.newExecBuilder().service(queryExecBuilderFactory).build();
        return new GraphQlExec<>(exec);
    }
}
