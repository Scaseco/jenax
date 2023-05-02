package org.aksw.jenax.arq.util.dataset;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.NodeUtils;

/** A DatasetGraphWrapper that delegates all find calls to {@link #actionFind(Node, Node, Node, Node)} */
public abstract class DatasetGraphWrapperFindBase
    extends DatasetGraphWrapper
{
    protected DatasetGraphWrapperFindBase(DatasetGraph dsg) {
        super(dsg);
    }

    /**
     * @implNote
     * The decision not to use Quad as the parameter type is to allow for more
     * succinct implementations by avoiding the need to call quad.getX() for the components.
     *
     * @param ng Controls the meaning of Node.ANY for g: If false then it matches
     *           all graphs including the default graph.
     *           When true it only matches within named graphs (and not the default graph).
     */
    protected abstract Iterator<Quad> actionFind(boolean ng, Node g, Node s, Node p, Node o);


    /** Use this method to delegate find calls to the wrapped dataset */
    protected Iterator<Quad> delegateFind(boolean ng, Node g, Node s, Node p, Node o) {
        return ng
            ? getR().findNG(g, s, p, o)
            : getR().find(g, s, p, o);
    }

    public Iterator<Quad> find(boolean ng, Node g, Node s, Node p, Node o) {
        Node gg = NodeUtils.nullToAny(g);
        Node ss = NodeUtils.nullToAny(s);
        Node pp = NodeUtils.nullToAny(p);
        Node oo = NodeUtils.nullToAny(o);
        return actionFind(ng, gg, ss, pp, oo);
    }

    @Override
    public Iterator<Quad> find() {
        return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return find(false, g, s, p, o);
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return find(true, g, s, p, o);
    }

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
