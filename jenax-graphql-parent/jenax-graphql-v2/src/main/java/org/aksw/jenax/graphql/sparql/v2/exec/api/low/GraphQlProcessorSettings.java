package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.apache.jena.graph.Node;

import graphql.language.Document;
import graphql.language.FragmentDefinition;
import graphql.parser.Parser;

public interface GraphQlProcessorSettings<X extends GraphQlProcessorSettings<X>> {
    default X document(String documentStr) {
        Document document = Parser.parse(documentStr);
        return document(document);
    }

    X document(Document document);
    X setVar(String name, Node value);

    X schemaNavigator(SchemaNavigator schemaNavigator);
    X nameToFragment(Map<String, FragmentDefinition> nameToFragment);

    default X setVars(Map<String, Node> vars) {
        for (Entry<String, Node> entry : vars.entrySet()) {
            setVar(entry.getKey(), entry.getValue());
        }
        return (X)this;
    }
}
