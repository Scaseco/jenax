package org.aksw.jenax.arq.sameas.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.arq.sameas.assembler.SameAsTerms;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public interface SameAsConfig
    extends Resource
{
    @Iri(SameAsTerms.baseDataset)
    Resource getBaseDataset();
    void setBaseDataset(Resource baseDataset);

    @Iri(SameAsTerms.NS + "predicate")
    Set<Node> getPredicates();

    @Iri(SameAsTerms.NS + "cacheMaxSize")
    Integer getCacheMaxSize();
    SameAsConfig setCacheMaxSize(Integer value);

    @Iri(SameAsTerms.NS + "allowDuplicates")
    Boolean getAllowDuplicates();
    SameAsConfig setAllowDuplicates(Boolean value);

    // @Iri(FromAsFilterTerms.NS + "alias")
    // @IriType
    // @KeyIri(FromAsFilterTerms.NS + "graph")
    // @ValueIri
    // Map<String, GraphAlias> getAliases();
}
