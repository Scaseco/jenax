package org.aksw.dcat.jena.domain.api;

import org.aksw.commons.model.maven.domain.api.MavenEntityCore;
import org.aksw.jenax.annotation.reprogen.Iri;
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
    @Override
    String getGroupId();
    @Override
    MavenEntity setGroupId(String groupId);

    @Iri(MvnTerms.artifactId)
    @Override
    String getArtifactId();
    @Override
    MavenEntity setArtifactId(String artifactId);

    @Iri(MvnTerms.version)
    @Override
    String getVersion();
    @Override
    MavenEntity setVersion(String version);

    @Iri(MvnTerms.type)
    @Override
    String getType();
    @Override
    MavenEntity setType(String type);

    @Iri(MvnTerms.classifier)
    @Override
    String getClassifier();
    @Override
    MavenEntity setClassifier(String classifier);
}
