package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.facete.v3.api.AliasedPath;
import org.aksw.jena_sparql_api.data_query.api.NodeAliasedPath;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

import com.google.common.collect.BiMap;

/**
 * NodeTransformer that detects nodes that are path references,
 * and resolves them to appropriate variables, thereby keeping
 * track of graph patterns that need to be injected into the
 * base query.
 *
 * Paths can be references as mandatory or as optional.
 *
 * <pre>
 *   <ul>
 *     <li>OPTIONAL { X } X -&gt; X</li>
 *     <li>OPTIONAL { HEAD } BODY OPTIONAL { TAIL } -&gt; should not happen, because if the body is mandatory, so should be head</li>
 *   </ul>
 * </pre>
 *
 * @author raven
 *
 * @param <T>
 */
public class NodeTransformResolvePaths
    implements NodeTransform
{
    protected ResolverNode resolver;
    protected Generator<Var> vargen;

    protected BiMap<Var, ResolverNode> varToResolver;

    // If a path is referenced as mandatory, all parents are marked as mandatory as well
    // Phase 1: Collection: Find all path references, mark parents as mandatory as needed
    // Phaso 2: Graph Pattern Generation: For each referenced path, create graph patterns for its parent elements as needed

//	protected

    public NodeTransformResolvePaths(ResolverNode resolver, Generator<Var> vargen,
            BiMap<Var, ResolverNode> varToResolver) {
        super();
        this.resolver = resolver;
        this.vargen = vargen;
        this.varToResolver = varToResolver;
    }

    @Override
    public Node apply(Node n) {
        Node result = n instanceof NodeAliasedPath
                ? $apply((NodeAliasedPath)n)
                : n;
        return result;
    }

    public Node $apply(NodeAliasedPath np) {
        AliasedPath ap = np.getPath();
        ResolverNode target = resolver.walk(ap);

        throw new RuntimeException("not implemented yet");
    }

}