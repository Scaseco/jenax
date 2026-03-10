package org.aksw.shellgebra.unused.algebra.dag;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

public class StreamEdge
    extends DefaultEdge
{
    private static final long serialVersionUID = 1L;

    private final String label;
    private final Map<String, Object> metadata;

    public StreamEdge() {
        this(null);
    }

    public StreamEdge(String label) {
        this.label = label;
        this.metadata = new HashMap<>();
    }

    @Override
    public StreamNode getSource() {
        return (StreamNode)super.getSource();
    }

    @Override
    public StreamNode getTarget() {
        return (StreamNode)super.getTarget();
    }


    public String getLabel() {
        return label;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Edge[" + getSource() + "->" + getTarget() + "]";
    }
}
