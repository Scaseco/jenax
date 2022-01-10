package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import org.apache.jena.graph.Node;

public interface PlainDataRefDcat
    extends PlainDataRef
{
    Node getDcatRecordNode();

    @Override
    default <T> T accept(PlainDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
