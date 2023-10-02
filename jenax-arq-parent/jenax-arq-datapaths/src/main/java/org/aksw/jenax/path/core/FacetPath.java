package org.aksw.jenax.path.core;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.graph.Node;

/** Paths for traversal of RDF data based on concrete properties IRIs and aliases. */
public class FacetPath
    extends PathBase<FacetStep, FacetPath>
{
    private static final long serialVersionUID = 1L;

    public FacetPath(PathOps<FacetStep, FacetPath> pathOps, boolean isAbsolute, List<FacetStep> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /** Convenience method to extract a path's nodes such as for retrieving labels */
    public Stream<Node> streamNodes() {
        return getSegments().stream()
                .filter(Objects::nonNull) // For robustness; should never be null
                .map(FacetStep::getNode);
    }

    /* Static convenience shorthands */

    public static FacetPath parse(String str) {
        return FacetPathOps.get().fromString(str);
    }

    public static FacetPath newAbsolutePath(FacetStep segment) {
        return FacetPathOps.get().newAbsolutePath(segment);
    }

    public static FacetPath newAbsolutePath(FacetStep ... segments) {
        return FacetPathOps.get().newAbsolutePath(segments);
    }

    public static FacetPath newAbsolutePath(List<FacetStep> segments) {
        return FacetPathOps.get().newAbsolutePath(segments);
    }

    public static FacetPath newRelativePath(FacetStep segment) {
        return FacetPathOps.get().newRelativePath(segment);
    }

    public static FacetPath newRelativePath(FacetStep ... segments) {
        return FacetPathOps.get().newRelativePath(segments);
    }

    public static FacetPath newRelativePath(List<FacetStep> segments) {
        return FacetPathOps.get().newRelativePath(segments);
    }
}
