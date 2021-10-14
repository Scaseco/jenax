package org.aksw.jenax.sparql.algebra.walker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOpsStr;
import org.apache.jena.sparql.algebra.Op;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


public class Tracker {
    protected Path<String> path;
    protected Map<Path<String>, Op> pathToOp;

    // Parent to child map can actually be computed on demand
    protected Multimap<Path<String>, Path<String>> parentToChildren;

    public Tracker() {
        super();
        this.path = PathOpsStr.newAbsolutePath();
        this.pathToOp = new LinkedHashMap<>();
        this.parentToChildren = LinkedHashMultimap.create();
    }

    public static Tracker create(Op rootOp) {
        Tracker result = new Tracker();
        result.getPathToOp().put(result.getPath(), rootOp);
        return result;
    }

    public Path<String> getPath() {
        return path;
    }

    public Map<Path<String>, Op> getPathToOp() {
        return pathToOp;
    }

    public Multimap<Path<String>, Path<String>> getParentToChildren() {
        return parentToChildren;
    }

    public void setPath(Path<String> path) {
        this.path = path;
    }

    public void setPathToOp(Map<Path<String>, Op> pathToOp) {
        this.pathToOp = pathToOp;
    }

    public void setParentToChildren(Multimap<Path<String>, Path<String>> parentToChildren) {
        this.parentToChildren = parentToChildren;
    }

}
