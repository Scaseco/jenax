package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.Set;

import org.aksw.jena_sparql_api.conjure.resourcespec.RpifTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

@ResourceView
public interface JobParam
    extends Resource
{
    @IriNs(RpifTerms.NS)
    Job getJob();
    JobParam setJob(Job job);

    @Iri("rdfs:label")
    String getParamName();
    JobParam setParamName(String name);

    // TODO dcterms:keyword is for strings; we want iri-based tags
    @IriNs("dcterms:keyword")
    @IriType
    Set<String> getTags();

    /**
     * A sparql expression to compute a default value for that param.
     * Sparql variables can be used to refer to values of other params.
     *
     * @return
     */
    @IriNs(RpifTerms.NS)
    Expr getDefaultValueExpr();
    JobParam setDefaultValueExpr(Expr defaultValueExprStr);


    @IriNs(RpifTerms.NS)
    Boolean isMandatory();
    JobParam setMandatory(Boolean isMandatory);

}
