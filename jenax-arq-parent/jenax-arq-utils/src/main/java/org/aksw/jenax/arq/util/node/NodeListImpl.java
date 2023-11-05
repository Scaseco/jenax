package org.aksw.jenax.arq.util.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ForwardingList;
import org.apache.jena.graph.Node;

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

    @Override
    protected List<Node> delegate() {
        return delegate;
    }
}
