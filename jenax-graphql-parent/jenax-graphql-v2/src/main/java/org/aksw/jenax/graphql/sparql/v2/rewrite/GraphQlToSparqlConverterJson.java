package org.aksw.jenax.graphql.sparql.v2.rewrite;

import org.aksw.jenax.graphql.sparql.v2.io.GraphQlIoBridge;
import org.apache.jena.graph.Node;

import graphql.language.Field;

public class GraphQlToSparqlConverterJson
    extends GraphQlToSparqlConverterBase<String>
{
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
