package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.aksw.dcat.jena.term.DcatTerms;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
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
