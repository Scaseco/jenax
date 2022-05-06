package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefGraph
    extends DataRef
{
    String getGraphIri();

    @Override
    default <T> T accept(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
