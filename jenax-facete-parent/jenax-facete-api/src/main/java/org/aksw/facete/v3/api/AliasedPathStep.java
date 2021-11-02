package org.aksw.facete.v3.api;

import org.aksw.jenax.sparql.relation.api.Relation;


/**
 * Thoughs about using specialized step types for variable-free expressions of paths:
 * - fwd(prop)
 * - optional - fwd - prop
 *
 * @author raven
 *
 */
public interface AliasedPathStep {
    // Whether the step is optional (left join))
    boolean isOptional();

    // if isFwd is true, this relation's target joins with the successor's source
    // otherwise, this relation's target will join with the successor's target;
    boolean isFwd();
    Relation getRelation();

    // Local alias of the step - resolution of the relation re-allocates the variables in regard to this alias
    String getAlias();
}
