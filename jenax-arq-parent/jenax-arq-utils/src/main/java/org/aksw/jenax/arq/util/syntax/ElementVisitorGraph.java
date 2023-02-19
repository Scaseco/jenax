package org.aksw.jenax.arq.util.syntax;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
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
    public void onVisit(Element el) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ElementTriplesBlock el)   {
        el.getPattern().forEach(result::add);
    }
}
