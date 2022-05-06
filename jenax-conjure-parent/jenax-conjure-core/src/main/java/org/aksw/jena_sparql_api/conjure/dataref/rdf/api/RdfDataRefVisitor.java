package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

public interface RdfDataRefVisitor<T>
{
    T visit(RdfDataRefEmpty dataRef);
    T visit(RdfDataRefUrl dataRef);
    T visit(RdfDataRefGraph dataRef);
    T visit(RdfDataRefCatalog dataRef);
    T visit(RdfDataRefSparqlEndpoint dataRef);
    T visit(RdfDataRefOp dataRef);
    T visit(RdfDataRefDcat dataRef);
    T visit(RdfDataRefGit dataRef);
    T visit(RdfDataRefExt dataRef);
}
