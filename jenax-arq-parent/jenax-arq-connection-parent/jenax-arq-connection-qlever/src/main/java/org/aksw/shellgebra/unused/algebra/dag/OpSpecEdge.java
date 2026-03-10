package org.aksw.shellgebra.unused.algebra.dag;

import org.jgrapht.graph.DefaultEdge;

public class OpSpecEdge
    extends DefaultEdge
{
    private static final long serialVersionUID = 1L;

    // private final String label;
    private final int index;
    // private final Map<String, Object> metadata;

    public OpSpecEdge() {
        this(0);
    }

    public OpSpecEdge(int index) {
        this.index = index;
    }

    //public StreamEdge(String label) {
    //    this.label = label;
    //    this.metadata = new HashMap<>();
    //}

    @Override
    public OpSpecNode getSource() {
        return (OpSpecNode)super.getSource();
    }

    @Override
    public OpSpecNode getTarget() {
        return (OpSpecNode)super.getTarget();
    }


    public int getIndex() {
        return index;
    }


    @Override
    public String toString() {
        return "Edge[" + getSource() + "->" + getTarget() + ": " + index + "]";
    }
}
