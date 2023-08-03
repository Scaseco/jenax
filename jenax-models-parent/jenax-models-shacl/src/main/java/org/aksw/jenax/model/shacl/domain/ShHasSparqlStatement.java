package org.aksw.jenax.model.shacl.domain;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface ShHasSparqlStatement
    extends ShHasPrefixes
{
    @Iri(ShTerms.select)
    String getSelectQueryString();
}
