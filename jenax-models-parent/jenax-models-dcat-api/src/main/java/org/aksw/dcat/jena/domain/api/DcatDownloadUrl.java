package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.aksw.dcat.jena.term.DcatTerms;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * A view of a download url that allows for inverse-traversal to corresponding
 * distributions and subsequent datasets.
 *
 * @author raven
 *
 */
@ResourceView
public interface DcatDownloadUrl
    extends Resource
{
    @Iri(DcatTerms.downloadURL)
    @Inverse
    Set<DcatDistribution> getDistributions();
}
