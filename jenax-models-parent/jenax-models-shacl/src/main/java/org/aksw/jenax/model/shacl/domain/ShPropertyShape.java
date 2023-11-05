package org.aksw.jenax.model.shacl.domain;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

public interface ShPropertyShape
    extends ShShape
{
    @Iri(ShTerms.path)
    Resource getPath();

    @Iri(ShTerms.uniqueLang)
    Boolean isUniqueLang();
    ShPropertyShape setUniqueLang(Boolean value);

    @Iri(ShTerms.minCount)
    Number getMinCount();
    ShShape setMinCount(Number number);

    @Iri(ShTerms.maxCount)
    Number getMaxCount();
    ShShape setMaxCount(Number number);

    @Iri(ShTerms.xclass)
    Set<Resource> getShaclClasses();
}
