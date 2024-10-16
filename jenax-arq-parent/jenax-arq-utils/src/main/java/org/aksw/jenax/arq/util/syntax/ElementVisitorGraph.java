package org.aksw.jenax.arq.util.syntax;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

public class ElementVisitorGraph
    extends ElementVisitorBaseChecked
{
    protected Graph result;

    public ElementVisitorGraph() {
        this(GraphFactory.createDefaultGraph());
    }

    public ElementVisitorGraph(Graph graph) {
        this.result = graph;
    }

    public Graph getGraph() {
        return result;
    }

    @Override
    protected void onVisit(Element el) {
        throw new UnsupportedOperationException("" + el.getClass());
    }

    @Override
    public void visit(ElementGroup el) { }

    @Override
    public void visit(ElementPathBlock el)   {
        el.getPattern().forEach(tp -> {
            Triple t = tp.asTriple();
            if (t == null) {
                throw new UnsupportedOperationException("TriplePath not supported: " + t);
            }
            result.add(t);
        });
    }

    @Override
    public void visit(ElementTriplesBlock el)   {
        el.getPattern().forEach(result::add);
    }
}
