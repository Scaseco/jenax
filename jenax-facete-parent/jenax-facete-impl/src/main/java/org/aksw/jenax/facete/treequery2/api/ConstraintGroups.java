package org.aksw.jenax.facete.treequery2.api;

import java.util.Set;

/**
 * Constraints within a group are disjunctive, whereas groups are conjunctive.
 */
public interface ConstraintGroups {
    Set<String> getConstraintGroupNames();
    // ConstraintGroup
}
