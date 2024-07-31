package org.aksw.jenax.io.json.graph;

import org.aksw.jenax.io.json.accumulator.AggJsonNode;

public interface GraphToJsonMapperNode
    extends GraphToJsonMapper
{
    GraphToJsonNodeMapperType getType();

    default GraphToJsonNodeMapperObject asObject() {
        return (GraphToJsonNodeMapperObject)this;
    }

    default GraphToJsonNodeMapperLiteral asLiteral() {
        return (GraphToJsonNodeMapperLiteral)this;
    }

    /**
     * Attempt to convert the mapper into an aggregator that can assemble
     * json from an ordered stream of triples.
     *
     * @throws UnsupportedOperationException if the conversion is unsupported
     */
    AggJsonNode toAggregator();
}
