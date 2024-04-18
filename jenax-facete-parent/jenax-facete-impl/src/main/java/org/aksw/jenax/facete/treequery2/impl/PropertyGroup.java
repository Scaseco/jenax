package org.aksw.jenax.facete.treequery2.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.MappedFragment;
import org.apache.jena.graph.Node;

/** Corresponds to a graphql fragment. Not to be confused with {@link Fragment}. */
public interface PropertyGroup {
    String getName();
    // Var var();

    // XXX setName?

    List<MappedFragment<Node>> getInjectFragments();

    PropertyGroup setFilterFragment(Fragment1 filterRelation);
    Fragment1 getFilterFragment();

    PropertyGroup getParent();

    ConstraintNode<NodeQuery> constraints();

    // NodeQuery sort(int sortDirection);
    // int getSortDirection();
    Map<FacetStep, RelationQuery> children();
    Collection<NodeQuery> getChildren();

    FacetStep reachingStep();
    PropertyGroup resolve(FacetPath facetPath);
    PropertyGroup getOrCreateChild(FacetStep step);

    RelationQuery relationQuery();
}
