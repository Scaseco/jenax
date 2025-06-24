package org.aksw.jenax.graphql.sparql.v2.api2;

/**
 * Base type for individual members of selection set.
 * Concrete sub classes are Field and FragmentSpread.
 */
public interface Selection
    extends ConnectiveNode
{
    String getName();
    // SelectionSet getParent();

    /** Create a builder from the state of this selection. */
    // FieldLikeBuilder toBuilder(SelectionSetBuilder<?> selectionSetBuilder);
}
