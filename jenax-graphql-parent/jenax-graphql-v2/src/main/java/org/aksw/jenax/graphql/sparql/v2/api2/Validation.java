package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;

public class Validation {
    public static void validateParentVars(BasicConnectInfo target, Collection<Var> finalParentVars) {
        Objects.requireNonNull(target);
        if (finalParentVars == null) {
            throw new RuntimeException("Parent variables not set.");
        }

        if (finalParentVars.isEmpty()) {
            throw new RuntimeException("Parent variable set is empty.");
        }

        Set<Var> absentParentVars = finalParentVars.stream().filter(x -> !target.getVisibleVars().contains(x)).collect(Collectors.toSet());
        if (!absentParentVars.isEmpty()) {
            throw new RuntimeException("Cannot connect to the following variables which do not exist in the parent: " + absentParentVars);
        }
    }
}
