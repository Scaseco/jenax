package org.aksw.commons.jena.jgrapht;

public interface LabeledEdgeFactory<V, E, T>
{
    E createEdge(V sourceVertex, V targetVertex, T label);
}