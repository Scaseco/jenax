package org.aksw.jenax.arq.fromasfilter.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.arq.fromasfilter.assembler.FromAsFilterTerms;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

public interface GraphAlias
    extends Resource
{
    @Iri(FromAsFilterTerms.NS + "graph")
    @IriType
    String getGraphIri();
    GraphAlias setGraphIri(String iriStr);

    @IriNs(FromAsFilterTerms.NS)
    Expr getExpr();
    GraphAlias setExpr(Expr expr);
}
