package org.aksw.jenax.io.json.mapper;

public interface RdfToJsonNodeMapper
    extends RdfToJsonMapper
{
    RdfToJsonNodeMapperType getType();

    default RdfToJsonNodeMapperObject asObject() {
        return (RdfToJsonNodeMapperObject)this;
    }

    default RdfToJsonNodeMapperLiteral asLiteral() {
        return (RdfToJsonNodeMapperLiteral)this;
    }
}
