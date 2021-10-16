package org.aksw.jenax.sparql.path;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

public interface PathVisitorRewrite
    extends PathVisitor
{
    Path getResult();
}
