package org.aksw.jenax.arq.util.dataset;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;

/** A DatasetGraphWrapper that delegates all find calls to {@link #actionFind(Node, Node, Node, Node)} */
public abstract class DatasetGraphWrapperFindBase
    extends DatasetGraphWrapper
{
    protected DatasetGraphWrapperFindBase(DatasetGraph dsg) {
        super(dsg);
    }

    /**
     * @param ngOnly Whether to restrict matching to named graphs only
     */
    protected abstract Iterator<Quad> actionFind(Node g, Node s, Node p, Node o);

    @Override public Iterator<Quad> find() { return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY); }
    @Override public Iterator<Quad> find(Node g, Node s, Node p, Node o) { return actionFind(g, s, p, o); }
    @Override public Iterator<Quad> find(Quad quad) { return actionFind(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }
    @Override public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) { return actionFind(g, s, p, o); }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        boolean result;
        Iterator<Quad> it = find(g, s, p, o);
        try {
            result = it.hasNext();
        } finally {
            Iter.close(it);
        }
        return result;
    }

    @Override public boolean contains(Quad quad) { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }
    @Override public Graph getDefaultGraph() { return GraphView.createDefaultGraph(this); }
    @Override public Graph getGraph(Node graphNode) { return GraphView.createNamedGraph(this, graphNode); }
    @Override public Graph getUnionGraph() { return GraphView.createUnionGraph(this); }
}
