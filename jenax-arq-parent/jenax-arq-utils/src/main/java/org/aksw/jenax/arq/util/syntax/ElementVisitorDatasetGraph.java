package org.aksw.jenax.arq.util.syntax;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/** Collect quads from the elements */
public class ElementVisitorDatasetGraph
    extends ElementVisitorBaseChecked
{
    protected DatasetGraph result;

    public ElementVisitorDatasetGraph() {
        this(DatasetGraphFactory.create());
    }

    public ElementVisitorDatasetGraph(DatasetGraph datasetGraph) {
        super();
        this.result = datasetGraph;
    }

    public DatasetGraph getDatasetGraph() {
        return result;
    }

    @Override
    public void onVisit(Element el) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ElementTriplesBlock el)   {
        el.getPattern().forEach(t -> result.add(new Quad(Quad.defaultGraphIRI, t)));
    }
}
