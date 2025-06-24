package org.aksw.jenax.engine.qlever;

import java.nio.file.Path;

public class QleverIndexBuilderConfigPojo
    implements QleverIndexBuilderConfig
{
    protected String dockerImageName;
    protected String dockerImageTag;

    protected Path outputFolder;
    protected String indexName;

    protected String stxxlMemory;

    @Override
    public String getDockerImageName() {
        return dockerImageName;
    }

    @Override
    public void setDockerImageName(String imageName) {
        this.dockerImageName = imageName;
    }

    @Override
    public String getDockerImageTag() {
        return dockerImageTag;
    }

    @Override
    public void setDockerImageTag(String imageTag) {
        this.dockerImageTag = imageTag;
    }

    @Override
    public Path getOutputFolder() {
        return outputFolder;
    }

    @Override
    public void setOutputFolder(Path path) {
        this.outputFolder = path;
    }

    @Override
    public void setOutputFolder(String pathStr) {
        Path path = Path.of(pathStr);
        setOutputFolder(path);
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String getStxxlMemory() {
        return stxxlMemory;
    }

    @Override
    public void setStxxlMemory(String stxxlMemory) {
        this.stxxlMemory = stxxlMemory;
    }
}
