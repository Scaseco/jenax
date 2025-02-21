package org.aksw.jenax.graphql.sparql;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;

import graphql.language.Document;
import graphql.language.Value;

public class GraphQlExecBuilderOverSparql
    extends RdfGraphQlExecBuilderFront
{
    protected RDFDataSource dataSource;
    protected GraphQlToSparqlMappingFactory mappingFactory;

    public GraphQlExecBuilderOverSparql(RDFDataSource dataSource, GraphQlToSparqlMappingFactory mappingFactory) {
        super();
        this.dataSource = dataSource;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public RdfGraphQlExec buildActual(Document document) {
        Map<String, Value<?>> map = GraphQlUtils.mapToGraphQl(assignments);

        GraphQlToSparqlMapping mapping = mappingFactory.newBuilder()
                // .setResolver(resolver)
                .setJsonMode(jsonMode)
                .setDocument(document)
                .setAssignments(map)
                .build();

        // GraphQlToSparqlMapping mapping = converter.convertDocument(document, map);
        RdfGraphQlExec result = new RdfGraphQlExecImpl(dataSource, mapping);
        return result;
    }
}
