package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.apache.jena.graph.Node;

import graphql.language.Field;
import graphql.language.FragmentDefinition;

public class GraphQlToSparqlConverterJson
    extends GraphQlToSparqlConverterBase<String>
{
    public GraphQlToSparqlConverterJson(Map<String, FragmentDefinition> nameToFragment,
            SchemaNavigator schemaNavigator) {
        super(nameToFragment, schemaNavigator);
    }

    @Override
    protected String toKey(Field field) {
        String result = XGraphQlUtils.fieldToJsonKey(field);
        return result;
    }

    @Override
    protected String toKey(Node node) {
        String result = GraphQlIoBridge.getPlainString(node);
        return result;
    }
}
