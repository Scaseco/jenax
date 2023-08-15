package org.aksw.jenax.model.shacl.domain;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;

public interface ShNodeShape
    extends ShShape
{
    @Iri(ShTerms.property)
    Set<ShPropertyShape> getProperties();

    @Iri(ShTerms.xone)
    List<ShNodeShape> getXone();

    @Iri(ShTerms.or)
    List<ShNodeShape> getOr();

    @Iri(ShTerms.and)
    List<ShNodeShape> getAnd();

    @Iri(ShTerms.not)
    Set<ShNodeShape> getNot();
}
