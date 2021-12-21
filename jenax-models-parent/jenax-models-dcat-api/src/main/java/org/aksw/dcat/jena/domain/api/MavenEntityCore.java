package org.aksw.dcat.jena.domain.api;

import java.util.List;

public interface MavenEntityCore {
    String getGroupId();
    MavenEntityCore setGroupId(String groupId);

    String getArtifactId();
    MavenEntityCore setArtifactId(String artifactId);

    String getVersion();
    MavenEntityCore setVersion(String version);

    List<String> getClassifiers();

//    String getClassifier();
//    MavenEntityCore setClassifier(String classifier);
}
