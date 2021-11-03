package org.aksw.jenax.arq.util.quad;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphMatcher;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.Sets;

public class CompareUtils {


    //public Set<Quad>
    public static Set<Quad> alignActualQuads(Set<Quad> expected, Set<Quad> actual) {
        Map<Node, Graph> e = QuadPatternUtils.indexAsGraphs(expected);
        Map<Node, Graph> a = QuadPatternUtils.indexAsGraphs(actual);

        Set<Quad> result = alignActualQuads(e, a);
        return result;
    }


    public static Set<Quad> toQuads(Node g, Graph graph) {
        Set<Quad> result = new HashSet<Quad>();
        ExtendedIterator<Triple> it = graph.find(null, null, null);
        try {
            while(it.hasNext()) {
                Triple t = it.next();

                Quad quad = new Quad(g, t);
                result.add(quad);
            }
        } finally {
            it.close();
        }

        return result;
    }

    public static Set<Quad> toQuads(Node g, Graph graph, Map<Node, Node> subst) {
        Set<Quad> result = new HashSet<Quad>();
        ExtendedIterator<Triple> it = graph.find(null, null, null);
        try {
            while(it.hasNext()) {
                Triple t = it.next();

                Quad tmp = new Quad(g, t);
                Quad quad = NodeTransformLib.transform(NodeTransformRenameMap.create(subst), tmp);

                result.add(quad);
            }
        } finally {
            it.close();
        }

        return result;
    }


    /**
     * Per graph alignment of quads.
     * Equivalent blank node objects in distinct graphs may be mapped differently.
     * Result of this method is not necessarily deterministic; see GraphMacher.match()
     *
     * @param expected
     * @param actual
     * @return
     */
    public static Set<Quad> alignActualQuads(Map<Node, Graph> expected, Map<Node, Graph> actual) {

        Set<Quad> result = new HashSet<Quad>();


//		Set<Quad> excessiveQuads = new HashSet<Quad>();
//		Set<Quad> missingQuads = new HashSet<Quad>();

        Set<Node> expectedGs = expected.keySet();
        Set<Node> actualGs = actual.keySet();

        Set<Node> excessiveGs = Sets.difference(actualGs, expectedGs);
        Set<Node> commonGs = Sets.intersection(expectedGs, actualGs);
//		Set<Node> missingGs = Sets.difference(expectedGs, actualGs);


        for(Node g : excessiveGs) {
            Graph graph = actual.get(g);
            Set<Quad> tmp = toQuads(g, graph);

            result.addAll(tmp);
        }

        for(Node g : commonGs) {
            Graph expectedGraph = expected.get(g);
            Graph actualGraph = actual.get(g);

            Node[][] rawMapping = GraphMatcher.match(actualGraph, expectedGraph);

            Map<Node, Node> mapping = new HashMap<Node, Node>();
            if(rawMapping != null) {
                for(int i = 0; i < rawMapping.length; ++i) {
                    Node source = rawMapping[i][0];
                    Node target = rawMapping[i][1];
                    mapping.put(source, target);
                }
            }
            else {
                //logger.warn("Could not establish a mapping between the graphs")
            }

            Set<Quad> tmp = toQuads(g, actualGraph, mapping);
            result.addAll(tmp);
        }

        return result;
    }

}
