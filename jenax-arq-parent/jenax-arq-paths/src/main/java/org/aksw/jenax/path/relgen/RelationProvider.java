package org.aksw.jenax.path.relgen;

import org.aksw.jenax.path.core.PathPE;
import org.aksw.jenax.sparql.relation.api.Relation;


/**
 * A supplier that sees all constraints on prior relations plus
 * the 'block' of constraints on its predecessor.
 *
 * @author raven
 *
 */
public interface RelationProvider {
    Relation getRelation(PathPE absPath, PathPE block);
}
