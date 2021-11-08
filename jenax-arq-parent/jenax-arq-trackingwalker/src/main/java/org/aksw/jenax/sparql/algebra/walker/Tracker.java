package org.aksw.jenax.sparql.algebra.walker;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOpsStr;
import org.apache.jena.sparql.algebra.Op;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class Tracker<T> {
    protected Path<String> path;
    protected Map<Path<String>, Op> pathToOp;

    /** Map for user data */
    protected Map<Path<String>, T> pathToData;

    // Parent to child map can actually be computed on demand
    protected Multimap<Path<String>, Path<String>> parentToChildren;

    public Tracker() {
        super();
        this.path = PathOpsStr.newAbsolutePath();
        this.pathToOp = new LinkedHashMap<>();
        this.parentToChildren = ArrayListMultimap.create();
        this.pathToData = new HashMap<>();
    }

    public static <T> Tracker<T> create(Op rootOp) {
        Tracker<T> result = new Tracker<>();
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

    public Map<Path<String>, T> getData() {
        return pathToData;
    }

    /** Attach a value to this tracker's current path */
    public void put(T value) {
        put(path, value);
    }

    /** Get this tracker's value for its current path */
    public T get() {
        return get(path);
    }

    public Path<String> getChildPath(int childIndex) {
        return getChildPath(path, childIndex);
    }

    public Path<String> getChildPath(Path<String> parent, int childIndex) {
        List<Path<String>> children = (List<Path<String>>)parentToChildren.get(parent);
        Path<String> result = children.get(childIndex);
        return result;
    }

//    public T getChild(Path<String> parent, int childIndex) {
//        List<Path<String>> children = parentToChildren.get(parent);
//        Path<String> child = children.get(childIndex);
//        T result = pathToData.get(child);
//        return result;
//    }

    public T computeIfAbsent(Function<? super Path<String>, ? extends T> fn) {
        return pathToData.computeIfAbsent(path, fn);
    }

    /** Attach a value to a path */
    public void put(Path<String> path, T value) {
        pathToData.put(path, value);
    }

    /** Get this tracker's value for a given path */
    public T get(Object path) {
        return pathToData.get(path);
    }

}
