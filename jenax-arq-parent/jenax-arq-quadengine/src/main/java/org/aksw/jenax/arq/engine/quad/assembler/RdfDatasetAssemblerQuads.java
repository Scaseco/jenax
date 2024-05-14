package org.aksw.jenax.arq.engine.quad.assembler;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.assembler.JA;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RdfDatasetAssemblerQuads
    extends Resource
{
    @Iri(JA.uri + "dataset")
    Resource getDataset();
    void setDataset(Resource baseDataset);

}
