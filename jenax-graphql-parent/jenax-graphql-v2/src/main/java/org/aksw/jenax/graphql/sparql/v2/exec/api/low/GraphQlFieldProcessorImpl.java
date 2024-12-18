package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.Objects;

public class GraphQlFieldProcessorImpl<K>
    implements GraphQlFieldProcessor<K>
{
    private GraphQlProcessor<K> processor;

    /** The field name from which this field processor was derived. */
    private String name;
    private QueryMapping<K> queryMapping;

    public GraphQlFieldProcessorImpl(String name, QueryMapping<K> queryMapping) {
        super();
        this.name = Objects.requireNonNull(name);
        this.queryMapping = Objects.requireNonNull(queryMapping);
    }

    @Override
    public GraphQlProcessor<K> getGraphQlProcessor() {
        return processor;
    }

    /** Sets the processor.
     * FIXME It is ugly setting the processor after the creation of this object. */
    void setGraphQlProcessor(GraphQlProcessor<K> processor) {
        this.processor = processor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GraphQlFieldExecBuilder<K> newExecBuilder() {
        return new GraphQlFieldExecBuilderImpl<>(processor, queryMapping);
    }
}
