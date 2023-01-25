package org.aksw.jenax.arq.util.dataset;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
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
}
