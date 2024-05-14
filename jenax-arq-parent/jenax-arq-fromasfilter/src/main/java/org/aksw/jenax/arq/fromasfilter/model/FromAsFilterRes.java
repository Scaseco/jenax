package org.aksw.jenax.arq.fromasfilter.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.arq.fromasfilter.assembler.FromAsFilterTerms;

@ResourceView
public interface FromAsFilterRes
    extends RdfAssemblerWrapper
{
    @Iri(FromAsFilterTerms.NS + "alias")
    Set<GraphAlias> getAliases();

    // @Iri(FromAsFilterTerms.NS + "alias")
    // @IriType
    // @KeyIri(FromAsFilterTerms.NS + "graph")
    // @ValueIri
    // Map<String, GraphAlias> getAliases();
}
