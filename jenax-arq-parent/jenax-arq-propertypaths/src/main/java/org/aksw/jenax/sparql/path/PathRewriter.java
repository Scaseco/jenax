package org.aksw.jenax.sparql.path;

import java.util.function.Function;

import org.apache.jena.sparql.path.Path;

public interface PathRewriter
    extends Function<Path, Path>
{
    Path apply(Path path);
}
