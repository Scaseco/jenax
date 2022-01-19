package org.aksw.dcat.mgmt.api;

import java.util.List;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.mgmt.vocab.DcatXTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

// @RdfType(DcatXTerms.DataProject)
@ResourceView
public interface DataProject
    extends RdfsLabel, RdfsComment, FoafDepiction
{
    @Iri("rdfs:member")
    <T extends Resource> List<T> getDatasets(Class<T> clz);

    @Iri("rdfs:member")
    List<DcatDataset> getDcatDatasets();

    @Iri(DcatXTerms.defaultGroupId)
    String getDefaultGroupId();
    DataProject setDefaultGroupId(String groupId);

    /* Overrides that enable fluent API style usage */

    @Override
    DataProject setLabel(String label);

    @Override
    DataProject setComment(String comment);

    @Override
    DataProject setDepiction(String url);

}
