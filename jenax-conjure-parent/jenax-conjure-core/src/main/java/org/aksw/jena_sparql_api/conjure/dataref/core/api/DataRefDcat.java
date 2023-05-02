package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import org.apache.jena.graph.Node;

public interface DataRefDcat
    extends DataRef
{
    Node getDcatRecordNode();

    @Override
    default <T> T accept(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
