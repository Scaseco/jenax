package org.aksw.jenax.facete.treequery2.api;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.facete.v3.api.VarScope;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;

// Can be deleted - superseded by ScopedFacetPath
public class ScopedFacetPathOld {
    protected VarScope scope;
    protected FacetPath facetPath;

    public ScopedFacetPathOld(VarScope scope, FacetPath facetPath) {
        super();
        this.scope = scope;
        this.facetPath = facetPath;
    }

    public ScopedFacetPathOld getParent() {
        return transformPath(FacetPath::getParent);
    }

    public VarScope getScope() {
        return scope;
    }

//    public String getScopeName() {
//        return scopeName;
//    }
//
//    public Var getStartVar() {
//        return startVar;
//    }

    public FacetPath getFacetPath() {
        return facetPath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, facetPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScopedFacetPathOld other = (ScopedFacetPathOld) obj;
        if (facetPath == null) {
            if (other.facetPath != null)
                return false;
        } else if (!facetPath.equals(other.facetPath))
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ScopedFacetPath [scope=" + scope + ", facetPath=" + facetPath + "]";
    }

    public static ScopedFacetPathOld of(VarScope scope, FacetPath facetPath) {
        return new ScopedFacetPathOld(scope, facetPath);
    }

    public static ScopedFacetPathOld of(Var startVar, FacetPath facetPath) {
        return of(VarScope.of(startVar), facetPath);
    }

    /**
     * Return a new ScopedFacetPath with the path transformed by the given function.
     * If the path is null then this function returns null.
     */
    public ScopedFacetPathOld transformPath(Function<? super FacetPath, ? extends FacetPath> facetPathFn) {
        FacetPath newPath = facetPathFn.apply(facetPath);
        return newPath == null ? null : new ScopedFacetPathOld(scope, newPath);
    }

    public ScopedVar toScopedVar(FacetPathMapping facetPathMapping) {
        return toScopedVar(this, facetPathMapping);
    }

    public static ScopedVar toScopedVar(ScopedFacetPathOld scopedFacetPath, FacetPathMapping facetPathMapping) {
        String scopeName = scopedFacetPath.getScope().getScopeName();
        Var startVar = scopedFacetPath.getScope().getStartVar();
        FacetPath facetPath = scopedFacetPath.getFacetPath();
        return FacetPathMappingImpl.resolveVar(facetPathMapping, scopeName, startVar, facetPath);
    }
}
