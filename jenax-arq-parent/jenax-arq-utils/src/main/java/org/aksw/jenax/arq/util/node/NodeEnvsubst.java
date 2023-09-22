package org.aksw.jenax.arq.util.node;

import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Envsubst (environment substitution) for nodes:
 * A {@link NodeTransform} with the following rules:
 *
 * By default, IRIs using the scheme 'env' are subject to substitution.
 *
 * Assuming a placeholder 'x' is mapped to 'http://example.org'
 * then applying envsubst to a node will yield a transformed node as follows
 *
 * <env:x> is mapped to the literal "http://example.org"
 * <env://x> is mapped to the iri <http://example.org>
 *
 */
public class NodeEnvsubst {
    // Prefix for URIs referring to environment variables
    public static final String ENV_PREFIX = "env:";


    /** Substitute placeholder nodes with corresponding values from a lookup map.
     * Non-placeholder nodes and placeholder nodes for which no substitution was specified
     * are left unchanged
     */
    public static Node subst(Node node, Function<String, String> lookup) {

        Entry<String, Boolean> e = getEnvKey(node);

        Node result = node;
        if(e != null) {
            String key = e.getKey();
            boolean isUri = e.getValue();
            String value = lookup.apply(key);
            if(value != null) {
                result = isUri
                    ? NodeFactory.createURI(value)
                    : NodeFactory.createLiteral(value);
            }

        }

        return result;
    }

    /** Substitute placeholders directly with the node obtained via lookup
     * Unmapped nodes are left unchanged
     * Note that if the placholder demanded an IRI then a substitution with a node will create
     * an IRI from its lexical form (this means literals will become IRIs).
     */
    public static Node substWithNode(Node node, Function<String, Node> lookup) {
        Entry<String, Boolean> e = getEnvKey(node);

        Node result = node;
        if(e != null) {
            String key = e.getKey();
            boolean isUri = e.getValue();
            Node value = lookup.apply(key);
            if(value != null) {
                result = isUri
                    ? NodeFactory.createURI(value.toString(false))
                    : value; // NodeFactory.createLiteral(value);
            }
        }

        return result;
    }


    /**
     * Return a pair (key, flag for string (false)/iri(true)) for nodes that reference
     * environment variables - null otherwise.
     *
     * @param node
     * @return
     */
    public static Entry<String, Boolean> getEnvKey(Node node) {
        Entry<String, Boolean> result = null;
        if(node.isURI()) {
            String str = node.getURI();
            if(str.startsWith(ENV_PREFIX)) {
                String key = str.substring(ENV_PREFIX.length());

                boolean isIri = false;
                if(key.startsWith("//")) {
                    key = key.substring(2);
                    isIri = true;
                }

                result = Maps.immutableEntry(key, isIri);
            }
        }

        return result;
    }


    public static boolean isEnvKey(Node node) {
        boolean result = getEnvKey(node) != null;
        return result;
    }

}
