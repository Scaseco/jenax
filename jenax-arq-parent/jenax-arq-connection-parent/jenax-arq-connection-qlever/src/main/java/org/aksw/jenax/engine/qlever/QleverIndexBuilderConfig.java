package org.aksw.jenax.engine.qlever;

import java.nio.file.Path;

public interface QleverIndexBuilderConfig {
    String getDockerImageName();
    void setDockerImageName(String imageName);

    String getDockerImageTag();
    void setDockerImageTag(String imageTag);

    Path getOutputFolder();
    void setOutputFolder(Path path);
    void setOutputFolder(String pathStr);

    String getIndexName();
    void setIndexName(String indexName);

    String getStxxlMemory();
    void setStxxlMemory(String memory);
}
