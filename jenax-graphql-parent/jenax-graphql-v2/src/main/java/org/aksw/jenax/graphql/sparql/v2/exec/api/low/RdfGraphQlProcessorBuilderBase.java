package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;

import graphql.language.Document;

public abstract class RdfGraphQlProcessorBuilderBase<K>
    implements RdfGraphQlProcessorBuilder<K>
{
    protected Document document;
    protected Map<String, Node> assignments = new LinkedHashMap<>(); // Binding?

    public RdfGraphQlProcessorBuilderBase() {
        super();
    }

    @Override
    public RdfGraphQlProcessorBuilder<K> document(Document document) {
        this.document = document;
        return this;
    }

    @Override
    public RdfGraphQlProcessorBuilder<K> setVar(String name, Node value) {
        this.assignments.put(name, value);
        return this;
    }
}
