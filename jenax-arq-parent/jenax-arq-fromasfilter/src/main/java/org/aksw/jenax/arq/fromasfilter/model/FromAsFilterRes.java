package org.aksw.jenax.arq.fromasfilter.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.arq.fromasfilter.assembler.FromAsFilterTerms;
import org.apache.jena.rdf.model.Resource;

public interface FromAsFilterRes
    extends Resource
{
    @Iri(FromAsFilterTerms.baseDataset)
    Resource getBaseDataset();
    void setBaseDataset(Resource baseDataset);

    @Iri(FromAsFilterTerms.NS + "alias")
    Set<GraphAlias> getAliases();

    // @Iri(FromAsFilterTerms.NS + "alias")
    // @IriType
    // @KeyIri(FromAsFilterTerms.NS + "graph")
    // @ValueIri
    // Map<String, GraphAlias> getAliases();
}
