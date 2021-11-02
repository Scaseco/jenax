package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.apache.jena.sparql.core.Var;

/**
 * Relationlets ("relation snippets") represent nested expression of joins of relations (graph patterns).
 *
 * The concept is as follows:
 * SPARQL graph patterns and SPARQL select queries intensionally describe a result set which can be seen
 * akin to an SQL relation (table).
 *
 * In SQL it is possible to express joins between individual relations by assigning them aliases and using
 * these aliases together with column names in join expressions, e.g:
 * <pre>SELECT * FROM departments AS a JOIN employees AS b ON (a.id = b.department_id)<pre>
 *
 * However, SPARQL does not provide such a mechanism for constructing JOINs between individual graph patterns.
 * This class enables construction of such SQL-like join expression.
 *
 * The model of relationlets can be SQL-like join expression:
 * relationlet1 AS alias1
 *   JOIN (relationlet2 AS alias2 JOIN relationlet3 AS alias3 ON (alias2.?y = alias3.?z)) AS alias4
 *   ON   (alias1.?s = alias4.?z)
 *
 * The method {@link #materialize()} yields for such an expression a single graph pattern with all the
 * book-keeping in place to find out which variable of which joining graph pattern was mapped to which variable
 * in the materialized one.
 *
 * Relationlets provide control over variable renaming and exposition:
 *
 * <ul>
 *   <li>Joins in this context implies appropriate variable renaming</li>
 *   <li>Variables can be pinned to prevent renaming them during materialization</li>
 *   <li>Joining on two pinned non-equal variables results raises an exception</li>
 *   <li>Specific variables in a graph pattern can be exposed under a global name (w.r.t. to this relationlet)
 *       so that the name can be used to refer to that variable</li>
 * </ul>
 *
 * Relevant sub classes are:
 * <ul>
 *   <li>{@link RelationletSimple} represents a single graph pattern and this type is also the result of materialization</li>
 *   <li>{@link RelationletJoinerImpl} represents a join expression of graph patterns</li>
 * </ul>
 *
 *
 * TODO The question is how Relation's distinguished vars translate to this class -
 * is it exposedVars? - not really; exposedVars is a Set whereas distinguished vars is a list
 *
 * @author raven
 *
 */
public interface Relationlet
{
    Collection<Var> getExposedVars();
    Set<Var> getVarsMentioned();

    default boolean isPinned(Var var) {
        Set<Var> pinnedVars = getPinnedVars();
        boolean result = pinnedVars.contains(var);
        return result;
    }

    default Relationlet pinVar(Var var) {
        return setPinnedVar(var, true);
    }

    /**
     * Adds all variables <b>currently</b> returned by getVarsMentioned() to the set of fixed vars.
     * Does not mark vars that become available in the future as fixed.
     *
     * @return
     */
    default Relationlet pinAllVars() {
        Set<Var> vars = getVarsMentioned();
        Relationlet result = pinAllVars(vars);
        return result;
    }

    default Relationlet pinAllVars(Iterable<Var> vars) {
        for(Var var : vars) {
            setPinnedVar(var, true);
        //return setVarFixed(var, true);
        }
        return this;
    }

    Set<Var> getPinnedVars();
    Relationlet setPinnedVar(Var var, boolean onOrOff);

    RelationletSimple materialize();

    NestedVarMap getNestedVarMap();

    default Var resolve(VarRefStatic varRef) {
        List<String> labels = varRef.getLabels();
        Var v = varRef.getV();

        NestedVarMap src = getNestedVarMap();
        NestedVarMap tgt = src.get(labels);
        Map<Var, Var> map = tgt.getLocalToFinalVarMap();

        Var result =  map.get(v);
        return result;
    }
}