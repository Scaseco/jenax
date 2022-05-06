package org.aksw.dcat.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * An interface with some very useful properties for identification and versioning
 * of arbitrary artifacts
 *
 * @author raven
 *
 */
@ResourceView
public interface MavenEntity
    extends Resource, MavenEntityCore
{
    @Iri(MvnTerms.groupId)
    String getGroupId();
    MavenEntity setGroupId(String groupId);

    @Iri(MvnTerms.artifactId)
    String getArtifactId();
    MavenEntity setArtifactId(String artifactId);

    @Iri(MvnTerms.version)
    String getVersion();
    MavenEntity setVersion(String version);

    @Iri(MvnTerms.type)
    String getType();
    MavenEntity setType(String type);

    @Iri(MvnTerms.classifier)
    String getClassifier();
    MavenEntity setClassifier(String classifier);
}
