package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.util.Objects;

public class GraphQlFieldProcessorImpl<K>
    implements GraphQlFieldProcessor<K>
{
    private String name;
    private QueryMapping<K> queryMapping;

    public GraphQlFieldProcessorImpl(String name, QueryMapping<K> queryMapping) {
        super();
        this.name = Objects.requireNonNull(name);
        this.queryMapping = Objects.requireNonNull(queryMapping);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GraphQlFieldExecBuilder<K> newExecBuilder() {
        return new GraphQlFieldExecBuilderImpl<>(queryMapping);
    }
}
