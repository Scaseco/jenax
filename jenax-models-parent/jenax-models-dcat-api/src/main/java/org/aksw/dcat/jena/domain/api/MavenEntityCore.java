package org.aksw.dcat.jena.domain.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface MavenEntityCore {
    String getGroupId();
    MavenEntityCore setGroupId(String groupId);

    String getArtifactId();
    MavenEntityCore setArtifactId(String artifactId);

    String getVersion();
    MavenEntityCore setVersion(String version);

    String getType();
    MavenEntityCore setType(String type);

    String getClassifier();
    MavenEntityCore setClassifier(String classifier);

    /** Return a copy where all nulls replaced by empty strings and all strings are trimmed */
    public static MavenEntityCore normalize(MavenEntityCore coord) {
        String g = Objects.toString(coord.getGroupId(), "").trim();
        String a = Objects.toString(coord.getArtifactId(), "").trim();
        String v = Objects.toString(coord.getVersion(), "").trim();
        String c = Objects.toString(coord.getClassifier(), "").trim();
        String t = Objects.toString(coord.getType(), "").trim();

        // If there is a classifier without a type then type becomes "jar"
        if (!c.isEmpty() && t.isEmpty()) {
            t = "jar";
        }

        return new MavenEntityCoreImpl(g, a, v, c, t);
    }

    public static String toString(MavenEntityCore coord) {
        String c = coord.getClassifier();
        String t = coord.getType();

        String suffix =
                (t.isEmpty() ? "" : ":" + t) +
                (c.isEmpty() ? "" : ":" + c);

        String result = coord.getGroupId() + ":" + coord.getArtifactId() + ":" + coord.getVersion() + suffix;
        return result;
    }

    public static String getFileNameSuffix(MavenEntityCore coord) {
        String c = coord.getClassifier();
        String t = coord.getType();

        String result =
                (c.isEmpty() ? "" : "-" + c) +
                (t.isEmpty() ? "" : "." + t);

        return result;
    }

    /**
     * Return a complete relative URL for the given coordinate, such as:
     * org/the/groupId/artifactId/version/artifactId-version-classifier.type
     */
    public static String toRelativeUrl(MavenEntityCore coord) {
        String result = toPath(coord) + "/" + toFileName(coord);
        return result;
    }

    /** Return the file name: artifactId-version-classifier.type */
    public static String toFileName(MavenEntityCore coord) {
        String suffix = getFileNameSuffix(coord);
        String result = coord.getArtifactId() + "-" + coord.getVersion() + suffix;
        return result;
    }

    /** Return the path fraction: org/the/groupId/artifactId/version */
    public static String toPath(MavenEntityCore coord) {
        String[] gs = coord.getGroupId().split("\\.");

        String g = Arrays.asList(gs).stream()
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.joining("/"));

        String result = g + "/" + coord.getArtifactId() + "/" + coord.getVersion();

        return result;
    }
}
