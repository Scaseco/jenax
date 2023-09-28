package org.aksw.jenax.facete.treequery2.api;

import java.util.Arrays;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathSysBase;
import org.aksw.facete.v3.api.VarScope;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;

public class ScopedFacetPath
    extends PathSysBase<FacetStep, ScopedFacetPath, VarScope>
{
    public ScopedFacetPath(VarScope system, Path<FacetStep> delegate) {
        super(system, delegate);
    }

    @Override
    protected ScopedFacetPath wrap(Path<FacetStep> basePath) {
        return of(system, basePath);
    }

    @Override
    public FacetPath getDelegate() {
        return (FacetPath)super.getDelegate();
    }

    /** Domain alias for getDelegate() - maybe undeprecate? */
    @Deprecated
    public FacetPath getFacetPath() {
        return getDelegate();
    }

    /** Domain alias for getSystem() - maybe undeprecate? */
    @Deprecated
    public VarScope getScope() {
        return getSystem();
    }

    @Override
    public String toString() {
        return getSystem() + ":" + getDelegate();
    }

    public static ScopedFacetPath of(VarScope system, Path<FacetStep> basePath) {
        return new ScopedFacetPath(system, basePath);
    }

    /** Convenience static shorthand for .get().newRoot() */
    public static ScopedFacetPath newAbsolutePath(VarScope scope, FacetStep ... segments) {
        return newAbsolutePath(scope, Arrays.asList(segments));
    }

    public static ScopedFacetPath newAbsolutePath(VarScope scope, List<FacetStep> segments) {
        return of(scope, FacetPathOps.get().newPath(true, segments));
    }

    public static ScopedFacetPath newRelativePath(VarScope scope, FacetStep ... segments) {
        return newRelativePath(scope, Arrays.asList(segments));
    }

    public static ScopedFacetPath newRelativePath(VarScope scope, List<FacetStep> segments) {
        return of(scope, FacetPathOps.get().newPath(false, segments));
    }

    public static ScopedFacetPath of(Var startVar, FacetPath facetPath) {
        return of(VarScope.of(startVar), facetPath);
    }

    /**
     * Return a new ScopedFacetPath with the path transformed by the given function.
     * If the path is null then this function returns null.
     */
//    public ScopedFacetPathOld transformPath(Function<? super FacetPath, ? extends FacetPath> facetPathFn) {
//        FacetPath newPath = facetPathFn.apply(facetPath);
//        return newPath == null ? null : new ScopedFacetPathOld(scope, newPath);
//    }

    public ScopedVar toScopedVar(FacetPathMapping facetPathMapping) {
        return toScopedVar(this, facetPathMapping);
    }

    public static ScopedVar toScopedVar(ScopedFacetPath scopedFacetPath, FacetPathMapping facetPathMapping) {
        String scopeName = scopedFacetPath.getSystem().getScopeName();
        Var startVar = scopedFacetPath.getSystem().getStartVar();
        FacetPath facetPath = (FacetPath)scopedFacetPath.getDelegate();
        return FacetPathMappingImpl.resolveVar(facetPathMapping, scopeName, startVar, facetPath);
    }

    public static void main(String[] args) {
        VarScope scope1 = VarScope.of("scope1", Vars.x);
        VarScope scope2 = VarScope.of("scope2", Vars.y);
        ScopedFacetPath path1 = ScopedFacetPath.newAbsolutePath(scope1);
        ScopedFacetPath path2 = ScopedFacetPath.newRelativePath(scope1).resolve(FacetStep.fwd(RDF.type.asNode()));
        ScopedFacetPath path3 = path1.resolve(path2);
        System.out.println(path3);
        System.out.println(path3.getParent());
        System.out.println(path3.resolve(path2));
    }
}
