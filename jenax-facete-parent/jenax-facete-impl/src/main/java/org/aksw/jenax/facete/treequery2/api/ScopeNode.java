package org.aksw.jenax.facete.treequery2.api;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

public class ScopeNode {
    protected Generator<String> scopeNameGenerator;
    protected TreeDataMap<FacetPath, String> pathToScope;

    protected FacetPath path;
    // protected FacetScopeGenerator context;

    protected ScopeNode parent;
    protected String scopeName;
    protected Var parentTargetVar;

    public ScopeNode(String scopeName, Var parentTargetVar) {
        this(null, scopeName, parentTargetVar);
    }

    public ScopeNode(ScopeNode parent, String scopeName, Var parentSourceVar) {
        this.parent = parent;
        this.scopeName = scopeName;
        this.parentTargetVar = parentSourceVar;
    }

    public Var getParentTargetVar() {
        return parentTargetVar;
    }

    public ScopeNode getParent() {
        return parent;
    }

    public String getScopeName() {
        return scopeName;
    }

    public ScopeNode newSubScope(String localId) {
        String newScopeName = scopeName + "_" + localId; // context.scopeNameGenerator.next();
        // TODO Link to this node as the parent? Probably we need two types of parents - one for the facet path, and one for the possibly nested entity from where a facet path started.
        return new ScopeNode(newScopeName, parentTargetVar);
    }

//    public ScopeNode resolve(FacetPath facetPath) {
//        return context.getOrCreateScope(this, facetPath);
//    }
//
//    public ScopeNode resolve(FacetStep facetStep) {
//        FacetPath child = path.resolve(facetStep);
//
//    }
}