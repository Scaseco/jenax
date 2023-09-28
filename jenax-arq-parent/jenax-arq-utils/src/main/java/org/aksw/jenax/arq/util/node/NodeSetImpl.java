package org.aksw.jenax.arq.util.node;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;
import org.apache.jena.graph.Node;

public class NodeSetImpl
    extends ForwardingSet<Node>
    implements NodeSet, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Set<Node> delegate;

    public NodeSetImpl() {
        this(new LinkedHashSet<>());
    }

    public NodeSetImpl(Set<Node> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected Set<Node> delegate() {
        return delegate;
    }
}
