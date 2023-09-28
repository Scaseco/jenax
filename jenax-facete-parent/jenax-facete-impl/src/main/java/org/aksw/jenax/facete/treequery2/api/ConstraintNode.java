package org.aksw.jenax.facete.treequery2.api;

import java.nio.charset.StandardCharsets;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.VarScope;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;

public interface ConstraintNode<R>
    extends RootedFacetTraversable<R, ConstraintNode<R>>
{
    ConstraintFacade<? extends ConstraintNode<R>> enterConstraints();
    
    public static ScopedFacetPath toScopedFacetPath(ConstraintNode<NodeQuery> constraintNode) {
    	
    	NodeQuery nodeQuery = constraintNode.getRoot();
    	FacetPath facetPath = nodeQuery.getFacetPath(); // TODO The facetPath must affect the scope
    	
    	RelationQuery relationQuery = nodeQuery.relationQuery();
    	FacetPathMapping pathMapping = relationQuery.getContext().getPathMapping();    	
    	String baseScope = relationQuery.getScopeBaseName();
    	String scopeContrib = pathMapping.allocate(facetPath);
    	
    	// TODO Probably this should be part of the PathMapping in order to allow for checking for hash clashes
    	String finalScope = FacetPathMappingImpl.toString(
    			FacetPathMappingImpl.DEFAULT_HASH_FUNCTION.newHasher()
    			.putString(baseScope, StandardCharsets.UTF_8)
    			.putString(scopeContrib, StandardCharsets.UTF_8)
    			.hash());
    	
    	FacetPath constraintPath = constraintNode.getFacetPath();
    	VarScope varScope = VarScope.of(finalScope, nodeQuery.var());
    	ScopedFacetPath scopedFacetPath = ScopedFacetPath.of(varScope, constraintPath);
    	return scopedFacetPath;
    }
}
