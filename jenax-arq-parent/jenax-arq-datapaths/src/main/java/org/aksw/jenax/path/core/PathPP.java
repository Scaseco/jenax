package org.aksw.jenax.path.core;

import java.util.List;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.sparql.path.P_Path0;


/** Path for SPARQL 1.1 property paths based on Jena's P_Path0 class */
public class PathPP
    extends PathBase<P_Path0, PathPP>
{
    private static final long serialVersionUID = 1L;

    public PathPP(PathOps<P_Path0, PathPP> pathOps, boolean isAbsolute, List<P_Path0> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /** Static convenience shorthands */
    public static PathPP newAbsolutePath(P_Path0 segment) {
        return PathOpsPP.get().newAbsolutePath(segment);
    }

    public static PathPP newAbsolutePath(P_Path0 ... segments) {
        return PathOpsPP.get().newAbsolutePath(segments);
    }

    public static PathPP newAbsolutePath(List<P_Path0> segments) {
        return PathOpsPP.get().newAbsolutePath(segments);
    }

    public static PathPP newRelativePath(P_Path0 segment) {
        return PathOpsPP.get().newRelativePath(segment);
    }

    public static PathPP newRelativePath(P_Path0 ... segments) {
        return PathOpsPP.get().newRelativePath(segments);
    }

    public static PathPP newRelativePath(List<P_Path0> segments) {
        return PathOpsPP.get().newRelativePath(segments);
    }
}
