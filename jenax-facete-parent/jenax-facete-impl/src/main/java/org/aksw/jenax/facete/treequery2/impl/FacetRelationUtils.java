package org.aksw.jenax.facete.treequery2.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class FacetRelationUtils {

    public static Map<Var, Node> createVarToComponentMap(Fragment relation) {
        List<Node> components = Arrays.asList(FacetStep.SOURCE, FacetStep.PREDICATE, FacetStep.TARGET);
        Map<Var, Node> result = components.stream()
                .map(c -> new SimpleEntry<>(resolveComponent(c, relation), c))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return result;
    }

    /** Map a component value of SOURCE, TARGET, PREDICATE */
    public static Var resolveComponent(Node component, Fragment relation) {
        Var g, s, p, o;
        List<Var> vars = relation.getVars();
        int n = vars.size();

        if (n == 1) {
            // source == target
            g = null; s = vars.get(0); p = null; o = vars.get(0);
        } else if (n == 2) {
            g = null; s = vars.get(0); p = null; o = vars.get(1);
        } else if (n == 3) {
            g = null; s = vars.get(0); p = vars.get(1); o = vars.get(2);
        } else {
            throw new RuntimeException("Binary or ternary relation expected");
        }

        Var result = null;
        if (FacetStep.isSource(component)) {
            result = s;
        } else if (FacetStep.isTarget(component)) {
            result = o;
        } else if (FacetStep.isPredicate(component)) {
            result = p;
        }

        return result;
    }


    public static Fragment renameVariables(Fragment relation, Var originalSubjectVar, Var renamedSubjectVar, String scopePrefix, Set<Var> forbiddenVars) {
        Set<Var> vars = relation.getVarsMentioned();
        vars.remove(originalSubjectVar);

        Map<Var, Var> map = new HashMap<>();
        map.put(originalSubjectVar, renamedSubjectVar);

        for (Var v : vars) {
            Var vv = Generator.create(scopePrefix + v.getName())
                    .map(Var::alloc)
                    .filterDrop(forbiddenVars::contains).next();
            map.put(v, vv);
        }

        Fragment result = relation.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(map::get));
        return result;
    }

}
