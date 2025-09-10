package org.aksw.jenax.arq.util.syntax;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class CanonicalRdf {
    public static List<Quad> canonicalizeQuads(List<Quad> quads) {
        List<Var> mentionedVars = quads.stream()
            .map(QuadUtils::getVarsMentioned)
            .flatMap(Collection::stream)
            .distinct()
            .toList();

        Map<Var, Var> relabel = createRelabelMap(mentionedVars);

        NodeTransform nodeTransform = new NodeTransformSubst(relabel);
        List<Quad> result = quads.stream()
            .map(quad -> NodeTransformLib.transform(nodeTransform, quad))
            .toList();
        return result;
    }

    public static List<Triple> canonicalizeTriples(List<Triple> triples) {
        List<Var> mentionedVars = triples.stream()
            .map(TripleUtils::getVarsMentioned)
            .flatMap(Collection::stream)
            .distinct()
            .toList();

        Map<Var, Var> relabel = createRelabelMap(mentionedVars);

        NodeTransform nodeTransform = new NodeTransformSubst(relabel);
        List<Triple> result = triples.stream()
            .map(quad -> NodeTransformLib.transform(nodeTransform, quad))
            .toList();
        return result;
    }

    public static Triple canonicalize(Triple t) {
        Set<Var> mentionedVars = TripleUtils.getVarsMentioned(t);
        Map<Var, Var> relabel = createRelabelMap(mentionedVars);

        NodeTransform nodeTransform = new NodeTransformSubst(relabel);
        Triple result = NodeTransformLib.transform(nodeTransform, t);
        return result;
    }

    public static Map<Var, Var> createRelabelMap(Collection<Var> mentionedVars) {
        Generator<Var> vargen = VarGeneratorImpl2.create("v");
        Map<Var, Var> relabel = mentionedVars.stream().collect(Collectors.toMap(v -> v, v -> vargen.next()));
        return relabel;
    }
}
