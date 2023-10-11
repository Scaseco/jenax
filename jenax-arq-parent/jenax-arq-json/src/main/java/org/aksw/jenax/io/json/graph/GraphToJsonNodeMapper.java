package org.aksw.jenax.io.json.graph;

public interface GraphToJsonNodeMapper
    extends GraphToJsonMapper
{
    GraphToJsonNodeMapperType getType();

    default GraphToJsonNodeMapperObject asObject() {
        return (GraphToJsonNodeMapperObject)this;
    }

    default GraphToJsonNodeMapperLiteral asLiteral() {
        return (GraphToJsonNodeMapperLiteral)this;
    }
}
