package org.aksw.commons.jena.jgrapht;

public interface LabeledEdge<V, T>
{
    V getSource();
    V getTarget();
    T getLabel();
    void setLabel(T label);
}