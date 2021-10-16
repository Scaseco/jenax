package org.aksw.jenax.sparql.path;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

public class PathWalker
{
    public static void walk(Path path, PathVisitor visitor)
    {
        walk(path, visitor, null, null) ;
    }

    public static void walk(Path path, PathVisitor visitor, PathVisitor beforeVisitor, PathVisitor afterVisitor)
    {
        PathVisitorWalker w = new PathVisitorWalker(visitor, beforeVisitor, afterVisitor) ;
        path.visit(w) ;
    }

//    protected static void walk$(Element el, EltWalker walker)
//    {
//        el.visit(walker) ;
//    }
}
