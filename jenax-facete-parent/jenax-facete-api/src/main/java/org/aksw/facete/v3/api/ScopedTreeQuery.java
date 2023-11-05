package org.aksw.facete.v3.api;

/** This class is intended to represent the "backbone" (= facet paths and their starting points) structure of FacetedRelationQuery - not sure if we really need it. */
public interface ScopedTreeQuery {
    ScopedTreeQueryNode root(VarScope scope);
}
