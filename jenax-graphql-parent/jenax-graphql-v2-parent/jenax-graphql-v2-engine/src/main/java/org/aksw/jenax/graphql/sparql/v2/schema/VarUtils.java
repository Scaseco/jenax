package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class VarUtils {
    /**
     * Returns a map that maps *each* variable from vbs to a name that does not appear in vas.
     *
     * @param excludeSymmetry if true, exclude mappings from a var in vbs to itself.
     */
    public static Map<Var, Var> createDistinctVarMap(Collection<Var> vas, Collection<Var> vbs, boolean excludeSymmetry, Generator<Var> generator) {

        // Ensure that the generator does not yield a forbidden variable
        Set<Var> forbidden = new HashSet<>();
        forbidden.addAll(vas);
        forbidden.addAll(vbs);
        generator = VarGeneratorBlacklist.create(generator, forbidden); //vas);

        // Rename all variables that are in common
        Map<Var, Var> result = new HashMap<Var, Var>();

        for(Var oldVar : vbs) {
            Var newVar;
            if (vas.contains(oldVar)) {
                newVar = generator.next();
            } else {
                newVar = oldVar;
            }

            boolean isSame = oldVar.equals(newVar);
            if(!(excludeSymmetry && isSame)) {
                result.put(oldVar, newVar);
            }
        }

        return result;
    }

    public static Map<Var, Var> createJoinVarMap(Collection<Var> sourceVars, Collection<Var> targetVars, List<Var> sourceJoinVars, List<Var> targetJoinVars, Generator<Var> generator) {

        if (sourceJoinVars.size() != targetJoinVars.size()) {
            throw new RuntimeException("Cannot join on different number of columns");
        }

        Map<Var, Var> result = VarUtils.createDistinctVarMap(sourceVars, targetVars, true, generator);

        for (int i = 0; i < sourceJoinVars.size(); ++i) {
            Var sourceJoinVar = sourceJoinVars.get(i);
            Var targetJoinVar = targetJoinVars.get(i);

            // Map targetVar to sourceVar
            result.put(targetJoinVar, sourceJoinVar);
            // rename[targetVar.getName()] = sourceVar;
        }

        return result;
    }

}
