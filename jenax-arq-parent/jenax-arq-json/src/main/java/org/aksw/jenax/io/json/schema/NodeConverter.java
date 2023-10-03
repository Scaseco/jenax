package org.aksw.jenax.io.json.schema;

public interface NodeConverter
    extends RdfToJsonConverter
{
    RdfToJsonConverterType getType();

    default NodeConverterObject asObject() {
        return (NodeConverterObject)this;
    }

    default NodeConverterLiteral asLiteral() {
        return (NodeConverterLiteral)this;
    }

//    default NodeSchemaArray asArray() {
//        return (NodeSchemaArray)this;
//    }
}
