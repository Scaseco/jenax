package org.aksw.jenax.arq.fromasfilter.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.arq.fromasfilter.assembler.FromAsFilterTerms;
import org.apache.jena.rdf.model.Resource;

// TODO Move to a common place (jenax-models-assembler?)
public interface RdfAssemblerWrapper
    extends Resource
{
    @Iri(FromAsFilterTerms.baseDataset)
    Resource getBaseDataset();
    void setBaseDataset(Resource baseDataset);
}
