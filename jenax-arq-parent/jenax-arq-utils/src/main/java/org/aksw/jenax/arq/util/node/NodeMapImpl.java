package org.aksw.jenax.arq.util.node;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.graph.Node;

import com.google.common.collect.ForwardingMap;

public class NodeMapImpl
    extends ForwardingMap<String, Node>
    implements NodeMap, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Map<String, Node> delegate;

    public NodeMapImpl() {
        this(new LinkedHashMap<>());
    }

    public NodeMapImpl(Map<String, Node> delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    protected Map<String, Node> delegate() {
        return delegate;
    }

//    public static NodeMap of(Map<String, Node> delegate) {
//        return new NodeMapImpl(delegate);
//    }
}
