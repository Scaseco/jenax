package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefVisitor<T> {
//	T visit(DataRefEmpty dataRef);

    T visit(DataRefUrl dataRef);
    T visit(DataRefGraph dataRef);

    T visit(DataRefCatalog dataRef);
    T visit(DataRefSparqlEndpoint dataRef);
    T visit(DataRefOp dataRef);

    T visit(DataRefDcat dataRef);
    T visit(DataRefGit dataRef);

    T visit(DataRefExt dataRef);
}
