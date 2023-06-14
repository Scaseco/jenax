package org.aksw.jenax.path.core;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.graph.Node;

/** Path for aliased SPARQL 1.1 property paths. */
public class PathPPA
    extends PathBase<AliasedStep, PathPPA>
{
    private static final long serialVersionUID = 1L;

    public PathPPA(PathOps<AliasedStep, PathPPA> pathOps, boolean isAbsolute, List<AliasedStep> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /** Convenience method to extract a path's nodes such as for retrieving labels */
    public Stream<Node> streamNodes() {
        return getSegments().stream()
                .filter(Objects::nonNull) // For robustness; should never be null
                .map(AliasedStep::getNode);
    }
}
