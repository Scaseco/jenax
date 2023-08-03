package org.aksw.jenax.model.shacl.domain;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;

public interface ShNodeShape
    extends ShShape
{
    @Iri(ShTerms.property)
    Set<ShPropertyShape> getProperties();

// FIXME Set<List<X>> not yet supported.
//     Set<List<@Iri("propertyForSubCollection") ShNodeShape>> getXone();

//    @Iri(ShTerms.xone)
//    Set<List<@ShNodeShape>> getXone();
//
//    @Iri(ShTerms.or)
//    Set<List<ShNodeShape>> getOr();
//
//    @Iri(ShTerms.and)
//    Set<List<ShNodeShape>> getAnd();

    @Iri(ShTerms.not)
    Set<ShNodeShape> getNot();
}
