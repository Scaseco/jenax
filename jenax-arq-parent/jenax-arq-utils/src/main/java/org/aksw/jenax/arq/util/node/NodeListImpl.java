package org.aksw.jenax.arq.util.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.jena.graph.Node;

import com.google.common.collect.ForwardingList;

public class NodeListImpl
    extends ForwardingList<Node>
    implements NodeList, Serializable
{
    private static final long serialVersionUID = 1L;

    protected List<Node> delegate;

    public NodeListImpl() {
        this(new ArrayList<>());
    }

    public NodeListImpl(List<Node> delegate) {
        super();
        this.delegate = delegate;
    }

    // 'copy' methods copy, 'wrap' methods wrap

    public static NodeList copyOf(Collection<Node> nodes) {
        return new NodeListImpl(new ArrayList<>(nodes));
    }

    public static NodeList copyOf(Node[] nodes) {
        return copyOf(Arrays.asList(nodes));
    }

    public static NodeList wrap(List<Node> nodes) {
        return new NodeListImpl(nodes);
    }

    public static NodeList wrap(Node[] nodes) {
        return wrap(Arrays.asList(nodes));
    }

    @Override
    protected List<Node> delegate() {
        return delegate;
    }
}
