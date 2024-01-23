package org.aksw.dcat.jena.domain.api;

public class MavenEntityCoreImpl
    implements MavenEntityCore
{
    protected String groupId;
    protected String artifactId;
    protected String version;
    protected String type;
    protected String classifier;
    protected String remainder;

    public MavenEntityCoreImpl() {
        super();
    }

    public MavenEntityCoreImpl(MavenEntityCore base) {
        this(base.getGroupId(), base.getArtifactId(), base.getVersion(), base.getType(), base.getClassifier(), base.getRemainder());
    }

    public MavenEntityCoreImpl(String groupId, String artifactId, String version, String type, String classifier, String remainder) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.remainder = remainder;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public MavenEntityCore setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public MavenEntityCore setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public MavenEntityCore setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public MavenEntityCore setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public MavenEntityCore setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getRemainder() {
        return remainder;
    }

    @Override
    public MavenEntityCore setRemainder(String remainder) {
        this.remainder = remainder;
        return this;
    }
}
