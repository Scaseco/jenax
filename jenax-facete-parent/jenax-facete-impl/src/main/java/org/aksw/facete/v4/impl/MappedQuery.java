package org.aksw.facete.v4.impl;

import java.util.Map;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

/**
 * A query with information about which variable corresponds to which path
 *
 */
// FIXME This should go into the DataQuery api - but DataQuery currently lacks the new PathPPA support.
public class MappedQuery {
    protected Query query;

    /** These are the paths that have a corresponding variable in the query's projection */
    protected BiMap<Var, ScopedFacetPath> varToPath;
    protected TreeData<ScopedFacetPath> tree;

    public MappedQuery(TreeData<ScopedFacetPath> tree, Query query, BiMap<Var, ScopedFacetPath> varToPath) {
        super();
        this.tree = tree;
        this.query = query;
        this.varToPath = varToPath;
    }

    public TreeData<ScopedFacetPath> getTree() {
        return tree;
    }

    public BiMap<Var, ScopedFacetPath> getVarToPath() {
        return varToPath;
    }

    public Map<ScopedFacetPath, Var> getPathToVar() {
        return varToPath.inverse();
    }

    public Query getQuery() {
        return query;
    }
}
