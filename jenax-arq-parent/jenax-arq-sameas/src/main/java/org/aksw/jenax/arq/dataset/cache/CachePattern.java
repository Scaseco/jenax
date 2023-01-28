package org.aksw.jenax.arq.dataset.cache;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;


/**
 * This class is used to specify for which patterns invocations to {@link DatasetGraph#find(Node, Node, Node, Node)} should cache.
 * Instances of this class are created using {@link #create(Node, Node, Node, Node)}.
 * The special node constant {@code IN} can be used to mark components that must be <b>concrete</b> when find() is called.
 * In other words: Whereas Node.ANY matches any node, IN only matches concrete nodes.
 *
 * Example, the following two patterns are used to cache all outgoing and incoming quads for any
 * find() call where either (g, s) or (g, o) are concrete, the predicate must be owl:sameAs, and the remaining component must be Node.ANY.
 * <pre>
 * CachePattern.create(IN, IN, OWL.sameAs.asNode(), Node.ANY)
 * CachePattern.create(IN, Node.ANY, OWL.sameAs.asNode(), IN)
 * </pre>
 *
 */
public class CachePattern {
    public static final Var IN = Var.alloc("IN");

    /** The specified pattern - may make use of IN */
    protected Quad specPattern;

    /** The pattern to retrieve all matching quads - effectively all IN's are substitude with Node.ANY */
    protected Quad findPattern;

    /** The indices of the INs in the spec pattern */
    protected int[] inputs;

    protected CachePattern(Quad specPattern, Quad findPattern, int[] inputs) {
        this.specPattern = specPattern;
        this.findPattern = findPattern;
        this.inputs = inputs;
    }

    public Quad getSpecPattern() {
        return specPattern;
    }

    public Quad getFindPattern() {
        return findPattern;
    }

    /** Do not modify the returned array */
    public int[] getInputs() {
        return inputs; //.clone() ?
    }

    public Tuple<Node> createPartitionKey(Quad quad) {
        Node[] arr = new Node[inputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            int idx = inputs[i];
            arr[i] = QuadUtils.getNode(quad, idx);
        }
        Tuple<Node> result = TupleFactory.create(arr);
        return result;
    }

    public Tuple<Node> createPartitionKey(Node mg, Node ms, Node mp, Node mo) {
        return createPartitionKey(QuadUtils.createMatch(mg, ms, mp, mo));
    }

    /**
     * Determine whether this pattern can match the given pattern's find() result.
     * <ul>
     *   <li>All inputs must be concrete</li>
     *   <li>Node.ANY only matches Node.ANY (rather than matching everything)</li>
     * <ul>
     */
    public boolean matchesPattern(Node mg, Node ms, Node mp, Node mo) {
        Node[] argNodes = new Node[] { mg, ms, mp, mo };
        boolean result = IntStream.range(0, argNodes.length).allMatch(i -> {
            Node pn = QuadUtils.getNode(specPattern, i);
            Node argNode = argNodes[i];
            boolean r = matchesPatternNode(pn, argNode);
            return r;
        });
        return result;
    }

    public static boolean matchesPatternNode(Node patternNode, Node argNode) {
        boolean result =
                IN.equals(patternNode)
                    ? argNode.isConcrete()
                    : Node.ANY.equals(patternNode)
                        ? Node.ANY.equals(argNode)
                        : patternNode.equals(argNode);
        return result;
    }

    /**
     * Marking components as IN only matches concrete values.
     *
     * create(IN, IN, owl:sameAs, OUT)
     *
     * @param g
     * @param s
     * @param p
     * @param o
     */
    public static CachePattern create(Node g, Node s, Node p, Node o) {
        Quad specPattern = QuadUtils.createMatch(g, s, p, o);
        int[] inputs = IntStream.range(0, 4).filter(i -> IN.equals(QuadUtils.getNode(specPattern, i))).toArray();
        Quad findPattern = QuadUtils.create(QuadUtils.streamNodes(specPattern).map(n -> IN.equals(n) ? Node.ANY : n).toArray(Node[]::new));
        return new CachePattern(specPattern, findPattern, inputs);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(inputs);
        result = prime * result + Objects.hash(specPattern);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CachePattern other = (CachePattern) obj;
        return Arrays.equals(inputs, other.inputs) && Objects.equals(specPattern, other.specPattern);
    }

    @Override
    public String toString() {
        return "CachePattern [pattern=" + specPattern + ", inputs=" + Arrays.toString(inputs) + "]";
    }
}
