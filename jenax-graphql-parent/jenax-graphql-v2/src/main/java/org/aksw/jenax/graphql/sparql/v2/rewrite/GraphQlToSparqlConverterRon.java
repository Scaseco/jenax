package org.aksw.jenax.graphql.sparql.v2.rewrite;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

import graphql.language.Field;

public class GraphQlToSparqlConverterRon
    extends GraphQlToSparqlConverterBase<P_Path0>
{
    @Override
    protected P_Path0 toKey(Field field) {
        P_Path0 result = XGraphQlUtils.fieldToRonKey(field);
        return result;
    }

    @Override
    protected P_Path0 toKey(Node node) {
        return new P_Link(node);
    }
}
